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

import org.gradle.api.Plugin
import org.gradle.api.Project
import release.github.GitHub
import release.github.GitHubHttp
import release.github.GitHubHttp.AuditEvent.RequestCompleted
import release.github.GitHubHttp.AuditEvent.RequestFailed
import release.github.formatFailure
import release.pki.ReleaseTrustStore

class ReleasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.version = determineVersion(target).also {
            target.logger.info("Using version $it")
        }
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

    private fun determineVersion(target: Project) = when (val versionFromEnvironment = System.getenv("SVG2ICO_VERSION")) {
        null -> when (val latestReleaseVersionOutcome =
            GitHubHttp(GitHubHttp.GitHubApiAuthority.productionGitHubApi, ReleaseTrustStore.defaultReleaseTrustStore) { auditEvent ->
                when (auditEvent) {
                    is RequestCompleted -> target.logger.info("Completed request to ${auditEvent.uri} with status code ${auditEvent.statusCode} and body ${auditEvent.responseBody}")
                    is RequestFailed -> target.logger.info("Failed request to ${auditEvent.uri} with exception", auditEvent.cause)
                }
            }.latestReleaseVersion()) {
            is GitHub.ReleaseVersionOutcome.Failure -> {
                target.logger.warn("Defaulting to development version: getting latest GitHub release failed: " + formatFailure(latestReleaseVersionOutcome.failure))
                VersionNumber.DevelopmentVersion
            }

            is GitHub.ReleaseVersionOutcome.Success -> latestReleaseVersionOutcome.versionNumber.increment()
        }

        else -> VersionNumber.fromString(versionFromEnvironment)
    }

}