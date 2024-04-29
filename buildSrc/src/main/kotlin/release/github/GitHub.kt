/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package release.github

import release.VersionNumber.ReleaseVersion
import java.nio.file.Path

interface GitHub {

    fun release(versionNumber: ReleaseVersion): ReleaseOutcome
    fun uploadArtifact(versionNumber: ReleaseVersion, releaseId: ReleaseId, path: Path): UploadArtifactOutcome

    data class ReleaseId(val value: String)
    sealed interface ReleaseOutcome {
        data class Success(val releaseId: ReleaseId): ReleaseOutcome
        data class Failure(val failureMessage: String): ReleaseOutcome
    }

    sealed interface UploadArtifactOutcome {
        object Success : UploadArtifactOutcome
        data class Failure(val failureMessage: String) : UploadArtifactOutcome
    }
}