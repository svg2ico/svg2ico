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

import argo.JsonGenerator
import argo.JsonParser
import argo.jdom.JsonNodeFactories.*
import net.sourceforge.urin.Authority
import net.sourceforge.urin.Authority.authority
import net.sourceforge.urin.Host.registeredName
import net.sourceforge.urin.Path.path
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameter
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameters
import net.sourceforge.urin.scheme.http.Https.https
import release.VersionNumber.ReleaseVersion
import release.github.GitHubHttp.GitHubApiAuthority
import release.github.PrivilegedGitHub.*
import release.pki.ReleaseTrustStore
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Path

class PrivilegedGitHubHttp(gitHubApiAuthority: GitHubApiAuthority, private val gitHubUploadAuthority: GitHubUploadAuthority, releaseTrustStore: ReleaseTrustStore, private val gitHubToken: GitHubToken) : PrivilegedGitHub {

    private val releasesUri = https(gitHubApiAuthority.authority, path("repos", "svg2ico", "svg2ico", "releases")).asUri()
    private val httpClient = HttpClient.newBuilder().sslContext(releaseTrustStore.sslContext).build()

    override fun release(versionNumber: ReleaseVersion) = httpClient.send(
            HttpRequest.newBuilder(releasesUri)
                .POST(
                    BodyPublishers.ofString(
                        JsonGenerator().generate(`object`(field("tag_name", string(versionNumber.toString()))))
                    )
                )
                .setHeader("content-type", "application/json")
                .setHeader("accept", "application/vnd.github+json")
                .setHeader("authorization", "Bearer $gitHubToken")
                .setHeader("x-github-api-version", "2022-11-28")
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).let { response ->
            if (response.statusCode() != 201) {
                ReleaseOutcome.Failure("Creating GitHub release via {$releasesUri} resulted in response code ${response.statusCode()} with body\n${response.body()}")
            } else {
                val releaseId = JsonParser().parse(response.body()).getNumberValue("id")
                ReleaseOutcome.Success(ReleaseId(releaseId))
            }
        }

    override fun uploadArtifact(versionNumber: ReleaseVersion, releaseId: ReleaseId, path: Path): UploadArtifactOutcome {
        val uploadUri = https(
            gitHubUploadAuthority.authority,
            path("repos", "svg2ico", "svg2ico", "releases", releaseId.value, "assets"),
            queryParameters(
                queryParameter("name", "svg2ico-${versionNumber}.jar"),
                queryParameter("label", "Jar")
            )
        ).asUri()
        return httpClient.send(
                HttpRequest.newBuilder(uploadUri)
                    .POST(BodyPublishers.ofFile(path))
                    .setHeader("content-type", "application/java-archive")
                    .setHeader("accept", "application/vnd.github+json")
                    .setHeader("authorization", "Bearer $gitHubToken")
                    .setHeader("x-github-api-version", "2022-11-28")
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).let { response ->
            if (response.statusCode() != 201) {
                UploadArtifactOutcome.Failure("Adding jar to GitHub release via {$uploadUri} resulted in response code ${response.statusCode()} with body\n${response.body()}")
            } else {
                UploadArtifactOutcome.Success
            }
        }
    }

    data class GitHubUploadAuthority(val authority: Authority) {
        companion object {
            val productionGitHubUpload =  GitHubUploadAuthority(authority(registeredName("upload.github.com")))
        }
    }

    data class GitHubToken(val token: String)
}