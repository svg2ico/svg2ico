/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package release

import com.sshtools.client.SshClient
import com.sshtools.client.shell.ExpectShell
import com.sshtools.client.tasks.ShellTask.ShellTaskBuilder
import com.sshtools.client.tasks.UploadFileTask.UploadFileTaskBuilder
import com.sshtools.common.ssh.Channel
import com.sshtools.common.ssh.ChannelEventListener
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.US_ASCII

abstract class SourceforgeReleaseTask : DefaultTask() {

    @get:InputFile
    abstract val jar: RegularFileProperty

    @get:InputFile
    abstract val documentationTar: RegularFileProperty

    @TaskAction
    fun release() {
        if (project.version == VersionNumber.DevelopmentVersion) {
            throw GradleException("Cannot release development version")
        }
        val username = "${project.property("sourceforgeUser")},svg2ico"
        val password = project.property("sourceforgePassword").toString().toCharArray()
        try {
            retrying {
                SshClient.SshClientBuilder.create()
                    .withHostname("shell.sourceforge.net")
                    .withPort(22)
                    .withUsername(username)
                    .withPassword(password)
                    .build()
            }.use {
                it.execute("mkdir --parents /home/frs/project/svg2ico/${project.version}")
            }
            retrying {
                SshClient.SshClientBuilder.create()
                    .withHostname("web.sourceforge.net")
                    .withPort(22)
                    .withUsername(username)
                    .withPassword(password)
                    .build()
            }.use {
                it.executePut(documentationTar, "/home/project-web/svg2ico/documentation-${project.version}.tgz")
                it.executePut(jar, "/home/frs/project/svg2ico/${project.version}/svg2ico-${project.version}.jar")
            }
            retrying {
                SshClient.SshClientBuilder.create()
                    .withHostname("shell.sourceforge.net")
                    .withPort(22)
                    .withUsername(username)
                    .withPassword(password)
                    .build()
            }.use {
                it.execute(
                    "mkdir --parents /home/project-web/svg2ico/${project.version}",
                    "tar --extract --verbose --file=/home/project-web/svg2ico/documentation-${project.version}.tgz --directory=/home/project-web/svg2ico/${project.version}",
                    "rm /home/project-web/svg2ico/documentation-${project.version}.tgz",
                    "rm /home/project-web/svg2ico/htdocs",
                    "ln --symbolic /home/project-web/svg2ico/${project.version} /home/project-web/svg2ico/htdocs"
                )
            }
        } catch (e: SshExecuteRuntimeException) {
            logger.error("Remote command execution failed", e)
            e.commandOutput?.also {
                logger.error("Command output:")
                logger.error(it)
            }
            logger.error("Full session output:")
            logger.error(e.sessionOutput)
            throw e
        }

        val defaultDownloadUri =
            URI.create("https://sourceforge.net/projects/svg2ico/files/${project.version}/svg2ico-${project.version}.jar")
        val response = HttpClient.newHttpClient()
                .send(
                        HttpRequest.newBuilder(defaultDownloadUri)
                                .PUT(HttpRequest.BodyPublishers.ofString("default=windows&default=mac&default=linux&default=bsd&default=solaris&default=others&download_label=${project.version}%20with%20source&api_key=${project.property("sourceforgeApiKey")}"))
                                .setHeader("content-type", "application/x-www-form-urlencoded")
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                )
        if (response.statusCode() < 200 || response.statusCode() >= 400) {
            throw GradleException("updating SourceForge default download to {$defaultDownloadUri} resulted in response code ${response.statusCode()} with body\n${response.body()}")
        }

    }

    private fun SshClient.execute(vararg commands: String) {
        class SshExecuteInnerRuntimeException(message: String, val commandOutput: String? = null) : RuntimeException(message)
        val recordingChannelEventListener = RecordingChannelEventListener()
        try {
            runTask(
                ShellTaskBuilder.create()
                    .withClient(this)
                    .onBeforeOpen { _, session ->
                        session.addEventListener(recordingChannelEventListener)
                        if (!session.executeCommand("create").waitFor(60_000).isDoneAndSuccess) {
                            throw SshExecuteInnerRuntimeException("Failed to create shell")
                        }
                    }
                    .onTask { task, _ ->
                        val expectShell = ExpectShell(task)
                        commands.forEach { command ->
                            val shellProcess = expectShell.executeCommand(command, US_ASCII.name())
                            shellProcess.drain()
                            if (shellProcess.exitCode != 0) {
                                val cause = when (val exitCode = shellProcess.exitCode) {
                                    ExpectShell.EXIT_CODE_PROCESS_ACTIVE -> "process active"
                                    ExpectShell.EXIT_CODE_UNKNOWN -> "exit code unknown"
                                    else -> "exit code $exitCode"
                                }
                                throw SshExecuteInnerRuntimeException("Command $command failed with $cause", shellProcess.commandOutput)
                            }
                            logger.info(shellProcess.commandOutput)
                        }
                    }
                    .build()
            )
        } catch (e: SshExecuteInnerRuntimeException) {
            throw SshExecuteRuntimeException(e, e.commandOutput, recordingChannelEventListener.stdOut())
        }
    }

    private fun SshClient.executePut(localFile: Provider<RegularFile>, remotePath: String) {
        runTask(
            UploadFileTaskBuilder.create()
                .withClient(this)
                .withLocalFile(localFile.get().asFile)
                .withRemotePath(remotePath)
                .build()
        )
    }

    private fun <T> retrying(block: () -> T) = generateSequence { runCatching(block) }
            .filterIndexed { index, result -> index >= 5 || result.isSuccess }
            .first()
            .getOrThrow()

    private class RecordingChannelEventListener : ChannelEventListener {
        private val standardStringBuilder = StringBuilder()

        override fun onChannelDataIn(channel: Channel, buffer: ByteBuffer) {
            standardStringBuilder.append(US_ASCII.decode(buffer.asReadOnlyBuffer()))
        }

        fun stdOut(): String {
            return standardStringBuilder.toString()
        }
    }

    private class SshExecuteRuntimeException(cause: Exception, val commandOutput: String?, val sessionOutput: String) : RuntimeException(cause.message, cause)

}