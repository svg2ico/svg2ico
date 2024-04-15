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

import argo.JsonGenerator
import argo.jdom.JsonNodeFactories.*
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class GitHubReleaseTask : DefaultTask() {

    @TaskAction
    fun release() {
        val gitHubToken = project.property("gitHubToken").toString()

        val getReleasesUri = URI("https://api.github.com/repos/svg2ico/svg2ico/releases")

        val response = HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder(getReleasesUri)
                    .POST(HttpRequest.BodyPublishers.ofString(JsonGenerator().generate(`object`(
                        field("tag_name", string(project.version.toString()))
                    ))))
                    .setHeader("content-type", "application/json")
                    .setHeader("accept", "application/vnd.github+json")
                    .setHeader("authorization", "Bearer $gitHubToken")
                    .setHeader("x-github-api-version", "2022-11-28")
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            )
        if (response.statusCode() < 200 || response.statusCode() >= 400) {
            throw GradleException("Retrieving GitHub releases via {$getReleasesUri} resulted in response code ${response.statusCode()} with body\n${response.body()}")
        } else {
            logger.info("GitHub responded with status code {}", response.statusCode())
            logger.info(response.body())
        }
    }

}