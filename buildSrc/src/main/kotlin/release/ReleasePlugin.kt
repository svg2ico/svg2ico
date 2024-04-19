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

import argo.JsonParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ReleasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.version = determineVersion(target)
        val extension = target.extensions.create("releasing", ReleasePluginExtension::class.java)
        target.tasks.register("sourceforgeRelease", SourceforgeReleaseTask::class.java) {
            group = "publishing"
            jar.set(extension.jar)
            documentationTar.set(extension.documentationTar)
        }
        target.tasks.register("gitHubRelease", GitHubReleaseTask::class.java) {
            group = "publishing"
            jar.set(extension.jar)
        }
    }

    private fun determineVersion(target: Project) =
        System.getenv("SVG2ICO_VERSION")?.let { VersionNumber.fromString(it) }?: lookupVersionNumberOnGitHub(target)

    private fun lookupVersionNumberOnGitHub(target: Project): VersionNumber {
        val logger = target.logger
        val releasesUri = URI("https://api.github.com/repos/svg2ico/svg2ico/releases")

        return try {
            val response = HttpClient.newHttpClient()
                .send(
                    HttpRequest.newBuilder(releasesUri)
                        .GET()
                        .setHeader("content-type", "application/json")
                        .setHeader("accept", "application/vnd.github+json")
                        .setHeader("x-github-api-version", "2022-11-28")
                        .build(),
                    HttpResponse.BodyHandlers.ofString()
                )
            if (response.statusCode() != 200) {
                logger.warn("Defaulting to development version: getting previous GitHub release via {$releasesUri} resulted in response code ${response.statusCode()} with body\n${response.body()}")
                VersionNumber.DevelopmentVersion
            } else {
                JsonParser().parse(response.body()).getArrayNode().map { release ->
                    release.getStringValue("tag_name")
                }.map { releaseString ->
                    VersionNumber.fromString(releaseString)
                }.maxOf { it }.increment().also {
                    logger.info("Using version {}", it)
                }
            }
        } catch (e: Exception) {
            logger.warn(
                "Defaulting to development version due to failure to get latest GitHub version from $releasesUri",
                e
            )
            VersionNumber.DevelopmentVersion
        }
    }
}