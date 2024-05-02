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

import argo.JsonParser
import net.sourceforge.urin.Authority
import net.sourceforge.urin.Authority.authority
import net.sourceforge.urin.Host.registeredName
import net.sourceforge.urin.Path.path
import net.sourceforge.urin.scheme.http.Https.https
import release.VersionNumber
import release.github.GitHubHttp.AuditEvent.RequestCompleted
import release.pki.ReleaseTrustStore
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class GitHubHttp(gitHubApiAuthority: GitHubApiAuthority, releaseTrustStore: ReleaseTrustStore, private val auditor: Auditor<AuditEvent>) : GitHub {

    private val releasesUri = https(gitHubApiAuthority.authority, path("repos", "svg2ico", "svg2ico", "releases")).asUri()
    private val httpClient = HttpClient.newBuilder().sslContext(releaseTrustStore.sslContext).build()

    override fun latestReleaseVersion() = httpClient.send(
        HttpRequest.newBuilder(releasesUri)
            .GET()
            .setHeader("content-type", "application/json")
            .setHeader("accept", "application/vnd.github+json")
            .setHeader("x-github-api-version", "2022-11-28")
            .build(),
        HttpResponse.BodyHandlers.ofString()
    ).let { response ->
        auditor.event(RequestCompleted(releasesUri, response.statusCode(), response.body()))
        if (response.statusCode() != 200) {
            GitHub.ReleaseVersionOutcome.Failure("Creating GitHub release via {$releasesUri} resulted in response code ${response.statusCode()} with body\n${response.body()}")
        } else {
            GitHub.ReleaseVersionOutcome.Success(
                JsonParser().parse(response.body()).getArrayNode().map { release ->
                    release.getStringValue("tag_name")
                }.map { releaseString ->
                    VersionNumber.fromString(releaseString)
                }.maxOf { it }
            )
        }
    }

    data class GitHubApiAuthority(val authority: Authority) {
        companion object {
            val productionGitHubApi = GitHubApiAuthority(authority(registeredName("api.github.com")))
        }
    }

    sealed interface AuditEvent {
        data class RequestCompleted(val uri: URI, val statusCode: Int, val responseBody: String) : AuditEvent
    }

}