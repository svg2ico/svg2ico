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
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.sourceforge.urin.Authority
import net.sourceforge.urin.Authority.authority
import net.sourceforge.urin.Host.registeredName
import net.sourceforge.urin.Path.path
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameter
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameters
import net.sourceforge.urin.scheme.http.Https.https
import release.VersionNumber
import release.github.GitHubHttp.AuditEvent.RequestCompleted
import release.github.PrivilegedGitHub.*
import release.pki.ReleaseTrustStore
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.nio.file.Path
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class GitHubHttp(
    gitHubApiAuthority: GitHubApiAuthority,
    releaseTrustStore: ReleaseTrustStore,
    private val auditor: Auditor<AuditEvent>,
    private val connectTimeout: Duration = 1.seconds,
    private val firstByteTimeout: Duration = 2.seconds,
    private val endToEndTimeout: Duration = 2.seconds,
) : GitHub {

    private val releasesPath = path("repos", "svg2ico", "svg2ico", "releases")
    private val releasesUri = https(gitHubApiAuthority.authority, releasesPath).asUri()
    private val pagedReleasesUri = https(gitHubApiAuthority.authority, releasesPath, queryParameters(queryParameter("per_page", "1"))).asUri()
    private val httpClient = HttpClient.newBuilder().sslContext(releaseTrustStore.sslContext).connectTimeout(connectTimeout.toJavaDuration()).build()

    fun privileged(gitHubUploadAuthority: GitHubUploadAuthority, gitHubToken: GitHubToken): PrivilegedGitHub = object: PrivilegedGitHub {

        override fun release(versionNumber: VersionNumber.ReleaseVersion) = httpClient.send(
            RequestBuilder
                .post(
                    releasesUri,
                    firstByteTimeout,
                    BodyPublishers.ofString(JsonGenerator().generate(`object`(field("tag_name", string(versionNumber.toString())))))
                )
                .withContentType("application/json")
                .withAuthorization(gitHubToken)
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

        override fun uploadArtifact(
            versionNumber: VersionNumber.ReleaseVersion,
            releaseId: ReleaseId,
            path: Path
        ): UploadArtifactOutcome {
            val uploadUri = https(
                gitHubUploadAuthority.authority,
                path("repos", "svg2ico", "svg2ico", "releases", releaseId.value, "assets"),
                queryParameters(
                    queryParameter("name", "svg2ico-${versionNumber}.jar"),
                    queryParameter("label", "Jar")
                )
            ).asUri()
            return httpClient.send(
                RequestBuilder
                    .post(uploadUri, firstByteTimeout, BodyPublishers.ofFile(path))
                    .withContentType("application/java-archive")
                    .withAuthorization(gitHubToken)
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

    }

    override fun latestReleaseVersion() = runBlocking(IO) {
        try {
            httpClient.sendAsync(
                RequestBuilder.get(pagedReleasesUri, firstByteTimeout).build(),
                HttpResponse.BodyHandlers.ofString()
            ).orTimeout(endToEndTimeout.inWholeMilliseconds, MILLISECONDS).await().let { response ->
                val responseHeaders = response.headers().map().flatMap { entry -> entry.value.map { entry.key to it } }
                runCatching {
                    auditor.event(RequestCompleted(
                        pagedReleasesUri,
                        response.statusCode(),
                        responseHeaders,
                        response.body()
                    ))
                    if (response.statusCode() != 200) {
                        GitHub.ReleaseVersionOutcome.Failure(
                            Failure.InvalidResponseCode(
                                pagedReleasesUri,
                                response.statusCode(),
                                200,
                                responseHeaders,
                                response.body()
                            )
                        )
                    } else {
                        GitHub.ReleaseVersionOutcome.Success(
                            JsonParser().parse(response.body()).getArrayNode().map { release ->
                                release.getStringValue("tag_name")
                            }.map { releaseString ->
                                VersionNumber.fromString(releaseString)
                            }.maxOf { it }
                        )
                    }
                }.getOrElse { exception ->
                    GitHub.ReleaseVersionOutcome.Failure(Failure.ResponseHandlingException(pagedReleasesUri, response.statusCode(), responseHeaders, response.body(), exception))
                }
            }
        } catch (exception: HttpConnectTimeoutException) {
            auditor.event(AuditEvent.RequestFailed(pagedReleasesUri, exception))
            GitHub.ReleaseVersionOutcome.Failure(Failure.ConnectTimeout(pagedReleasesUri, connectTimeout, exception))
        } catch (exception: HttpTimeoutException) {
            auditor.event(AuditEvent.RequestFailed(pagedReleasesUri, exception))
            GitHub.ReleaseVersionOutcome.Failure(Failure.FirstByteTimeout(pagedReleasesUri, firstByteTimeout, exception))
        } catch (exception: TimeoutException) {
            auditor.event(AuditEvent.RequestFailed(pagedReleasesUri, exception))
            GitHub.ReleaseVersionOutcome.Failure(Failure.EndToEndTimeout(pagedReleasesUri, endToEndTimeout, exception))
        } catch (exception: Exception) {
            auditor.event(AuditEvent.RequestFailed(pagedReleasesUri, exception))
            GitHub.ReleaseVersionOutcome.Failure(Failure.RequestSubmittingException(pagedReleasesUri, exception))
        }
    }

    data class GitHubApiAuthority(val authority: Authority) {
        companion object {
            val productionGitHubApi = GitHubApiAuthority(authority(registeredName("api.github.com")))
        }
    }

    data class GitHubUploadAuthority(val authority: Authority) {
        companion object {
            val productionGitHubUpload = GitHubUploadAuthority(authority(registeredName("upload.github.com")))
        }
    }

    data class GitHubToken(val token: String)

    sealed interface AuditEvent {
        data class RequestCompleted(val uri: URI, val statusCode: Int, val headers: List<Pair<String, String>>, val responseBody: String) : AuditEvent
        data class RequestFailed(val uri: URI, val cause: Throwable) : AuditEvent
    }

}