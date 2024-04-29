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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import release.github.GitHubHttp.GitHubApiAuthority.Companion.productionGitHubApi
import release.github.PrivilegedGitHub.ReleaseOutcome
import release.github.PrivilegedGitHub.UploadArtifactOutcome
import release.github.PrivilegedGitHubHttp
import release.github.PrivilegedGitHubHttp.GitHubToken
import release.github.PrivilegedGitHubHttp.GitHubUploadAuthority.Companion.productionGitHubUpload
import release.pki.ReleaseTrustStore.Companion.defaultReleaseTrustStore

abstract class GitHubReleaseTask : DefaultTask() {

    @get:InputFile
    abstract val jar: RegularFileProperty

    @TaskAction
    fun release() { // TODO logging
        when (val version = project.version) {
            is VersionNumber.DevelopmentVersion -> throw GradleException("Cannot release development version")
            is VersionNumber.ReleaseVersion -> {
                val gitHubToken = project.property("gitHubToken").toString()
                val privilegedGitHub = PrivilegedGitHubHttp(productionGitHubApi, productionGitHubUpload, defaultReleaseTrustStore, GitHubToken(gitHubToken))
                when (val releaseOutcome = privilegedGitHub.release(version)) {
                    is ReleaseOutcome.Success -> {
                        when (val uploadArtifactOutcome = privilegedGitHub.uploadArtifact(version, releaseOutcome.releaseId, jar.get().asFile.toPath())) {
                            UploadArtifactOutcome.Success -> Unit
                            is UploadArtifactOutcome.Failure -> throw GradleException(uploadArtifactOutcome.failureMessage)
                        }
                    }
                    is ReleaseOutcome.Failure -> throw GradleException(releaseOutcome.failureMessage)
                }
            }
        }
    }

}