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

abstract class SourceforgeReleaseTask : DefaultTask() {

    @get:InputFile
    abstract val jar: RegularFileProperty

    @get:InputFile
    abstract val documentationTar: RegularFileProperty

    @TaskAction
    fun release() {
        val sshClientBuilder = SshClient.SshClientBuilder.create()
            .withPort(22)
            .withUsername("${project.property("sourceforgeUser")},svg2ico")
            .withPassword(project.property("sourceforgePassword").toString().toCharArray())

        retrying { sshClientBuilder.withHostname("shell.sourceforge.net").build() }.use {
            it.execute("mkdir --parents /home/frs/project/svg2ico/${project.version}")
        }
        retrying { sshClientBuilder.withHostname("web.sourceforge.net").build() }.use {
            it.executePut(documentationTar, "/home/project-web/svg2ico/documentation-${project.version}.tgz")
            it.executePut(jar, "/home/frs/project/svg2ico/${project.version}/svg2ico-${project.version}.jar")
        }
        retrying { sshClientBuilder.withHostname("shell.sourceforge.net").build() }.use {
            it.execute(
                "mkdir --parents /home/project-web/svg2ico/${project.version}",
                "tar --extract --verbose --file=/home/project-web/svg2ico/documentation-${project.version}.tgz --directory=/home/project-web/svg2ico/${project.version}",
                "rm /home/project-web/svg2ico/documentation-${project.version}.tgz",
                "rm /home/project-web/svg2ico/htdocs",
                "ln --symbolic /home/project-web/svg2ico/${project.version} /home/project-web/svg2ico/htdocs"
            )
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
        runTask(
            ShellTaskBuilder.create()
                .withClient(this)
                .onBeforeOpen { _, session ->
                    if (!session.executeCommand("create").waitFor(60_000).isDoneAndSuccess) {
                        throw IllegalStateException("Failed to create shell")
                    }
                }
                .onTask { task, _ ->
                    val expectShell = ExpectShell(task)
                    commands.forEach { command ->
                        val shellProcess = expectShell.executeCommand(command)
                        shellProcess.drain()
                        when (val exitCode = shellProcess.exitCode) {
                            ExpectShell.EXIT_CODE_PROCESS_ACTIVE -> throw java.lang.Exception("Process active")
                            ExpectShell.EXIT_CODE_UNKNOWN -> throw java.lang.Exception("Exit code unknown")
                            0 -> Unit
                            else -> throw java.lang.Exception("Command $command failed with exit code $exitCode")
                        }
                    }
                }
                .build()
        )
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
}