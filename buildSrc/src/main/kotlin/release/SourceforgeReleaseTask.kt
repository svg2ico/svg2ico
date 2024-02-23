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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

open class SourceforgeReleaseTask : DefaultTask() {

    @TaskAction
    fun release() {
        val username = "${project.property("sourceforgeUser")},svg2ico"
        val password = project.property("sourceforgePassword").toString().toCharArray()
        retrying { SshClient("shell.sourceforge.net", 22, username, password) }.use {
            logger.info(it.executeCommand("create"))
            logger.info(it.executeCommand("execute \"mkdir -p /home/frs/project/svg2ico/svg2ico/${project.version}\""))
        }
        retrying { SshClient("web.sourceforge.net", 22, username, password) }.use {
            it.putFile(project.layout.buildDirectory.file("distributions/documentation-${project.version}.tgz").get().asFile, "/home/project-web/svg2ico/")
            it.putFile(project.layout.buildDirectory.file("libs/svg2ico-${project.version}-javadoc.jar").get().asFile, "/home/project-web/svg2ico/")
            it.putFile(project.layout.buildDirectory.file("libs/svg2ico-${project.version}.jar").get().asFile, "/home/frs/project/svg2ico/svg2ico/")
        }
        retrying { SshClient("shell.sourceforge.net", 22, username, password) }.use {
            logger.info(it.executeCommand("mkdir -p /home/project-web/svg2ico/${project.version}/javadoc && tar -xvf /home/project-web/svg2ico/documentation-${project.version}.tgz -C /home/project-web/svg2ico/${project.version} && unzip -d /home/project-web/svg2ico/${project.version}/javadoc /home/project-web/svg2ico/svg2ico-${project.version}-javadoc.jar && rm /home/project-web/svg2ico/documentation-${project.version}.tgz && rm /home/project-web/svg2ico/svg2ico-${project.version}-javadoc.jar && rm /home/project-web/svg2ico/htdocs ; ln -s /home/project-web/svg2ico/${project.version} /home/project-web/svg2ico/htdocs"))
        }

        val response = HttpClient.newHttpClient()
                .send(
                        HttpRequest.newBuilder(URI.create("https://sourceforge.net/projects/svg2ico/files/svg2ico/${project.version}//svg2ico-${project.version}.jar"))
                                .PUT(HttpRequest.BodyPublishers.ofString("default=windows&default=mac&default=linux&default=bsd&default=solaris&default=others&download_label=${project.version}%20with%20source&api_key=${project.property("sourceforgeApiKey")}"))
                                .setHeader("content-type", "application/x-www-form-urlencoded")
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                )
        if (response.statusCode() < 200 || response.statusCode() >= 400) {
            throw GradleException("updating SourceForge default download resulted in response code ${response.statusCode()} with body\n${response.body()}")
        }

    }

    private fun <T> retrying(block: () -> T) = generateSequence { runCatching(block) }
            .filterIndexed { index, result -> index >= 3 || result.isSuccess }
            .first()
            .getOrThrow()
}