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
import release.github.GitHub.ReleaseVersionOutcome
import release.github.GitHubHttp.AuditEvent.RequestCompleted
import release.github.PrivilegedGitHub.*
import release.pki.ReleaseTrustStore
import java.net.URI
import java.net.http.*
import java.net.http.HttpRequest.BodyPublishers
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

    private fun <T> executeRequest(request: HttpRequest, expectedResponseCode: Int, responseBodyHandler: (String) -> T) = runBlocking(IO) {
        try {
            httpClient.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString()
            ).orTimeout(endToEndTimeout.inWholeMilliseconds, MILLISECONDS).await().let { response ->
                val responseHeaders = response.headers().map().flatMap { entry -> entry.value.map { entry.key to it } }
                runCatching {
                    auditor.event(
                        RequestCompleted(
                            request.uri(),
                            response.statusCode(),
                            responseHeaders,
                            response.body()
                        )
                    )
                    if (response.statusCode() != expectedResponseCode) {
                        Outcome.Failure(
                            Failure.InvalidResponseCode(
                                request.uri(),
                                response.statusCode(),
                                expectedResponseCode,
                                responseHeaders,
                                response.body()
                            )
                        )
                    } else {
                        Outcome.Success(responseBodyHandler(response.body()))
                    }
                }.getOrElse { exception ->
                    Outcome.Failure(
                        Failure.ResponseHandlingException(
                            request.uri(),
                            response.statusCode(),
                            responseHeaders,
                            response.body(),
                            exception
                        )
                    )
                }
            }
        } catch (exception: HttpConnectTimeoutException) {
            auditor.event(AuditEvent.RequestFailed(request.uri(), exception))
            Outcome.Failure(Failure.ConnectTimeout(request.uri(), connectTimeout, exception))
        } catch (exception: HttpTimeoutException) {
            auditor.event(AuditEvent.RequestFailed(request.uri(), exception))
            Outcome.Failure(Failure.FirstByteTimeout(request.uri(), firstByteTimeout, exception))
        } catch (exception: TimeoutException) {
            auditor.event(AuditEvent.RequestFailed(request.uri(), exception))
            Outcome.Failure(Failure.EndToEndTimeout(request.uri(), endToEndTimeout, exception))
        } catch (exception: Exception) {
            auditor.event(AuditEvent.RequestFailed(request.uri(), exception))
            Outcome.Failure(Failure.RequestSubmittingException(request.uri(), exception))
        }
    }

    fun privileged(gitHubUploadAuthority: GitHubUploadAuthority, gitHubToken: GitHubToken): PrivilegedGitHub = object : PrivilegedGitHub {

        override fun release(versionNumber: VersionNumber.ReleaseVersion) = executeRequest(
            baseHttpRequestBuilder(releasesUri)
                .POST(BodyPublishers.ofString(JsonGenerator().generate(`object`(field("tag_name", string(versionNumber.toString()))))))
                .setHeader("content-type", "application/json")
                .setHeader("authorization", "Bearer ${gitHubToken.token}")
                .build(),
            201
        ) { ReleaseId(JsonParser().parse(it).getNumberValue("id")) }
            .let {
                when (it) {
                    is Outcome.Success -> ReleaseOutcome.Success(it.value)
                    is Outcome.Failure -> ReleaseOutcome.Failure(it.failure)
                }
            }


        override fun uploadArtifact(versionNumber: VersionNumber.ReleaseVersion, releaseId: ReleaseId, path: Path) = executeRequest(
            baseHttpRequestBuilder(
                https(
                    gitHubUploadAuthority.authority,
                    path("repos", "svg2ico", "svg2ico", "releases", releaseId.value, "assets"),
                    queryParameters(
                        queryParameter("name", "svg2ico-${versionNumber}.jar"),
                        queryParameter("label", "Ant task & command line package")
                    )
                ).asUri()
            )
                .POST(BodyPublishers.ofFile(path))
                .setHeader("content-type", "application/java-archive")
                .setHeader("authorization", "Bearer ${gitHubToken.token}")
                .build(),
            201
        ) { }
            .let {
                when (it) {
                    is Outcome.Success -> UploadArtifactOutcome.Success
                    is Outcome.Failure -> UploadArtifactOutcome.Failure(it.failure)
                }
            }

    }

    override fun latestReleaseVersion() = executeRequest(baseHttpRequestBuilder(pagedReleasesUri).GET().build(), 200) { responseBody ->
        JsonParser().parse(responseBody).getArrayNode().map { release ->
            release.getStringValue("tag_name")
        }.map { releaseString ->
            VersionNumber.fromString(releaseString)
        }.maxOf { it }
    }.let {
        when(it) {
            is Outcome.Success -> ReleaseVersionOutcome.Success(it.value)
            is Outcome.Failure -> ReleaseVersionOutcome.Failure(it.failure)
        }
    }

    private fun baseHttpRequestBuilder(uri: URI) = HttpRequest.newBuilder(uri)
        .setHeader("accept", "application/vnd.github+json")
        .setHeader("x-github-api-version", "2022-11-28")
        .setHeader("user-agent", "svg2ico-build")
        .timeout(firstByteTimeout.toJavaDuration())

    private sealed interface Outcome<T> {
        data class Success<T>(val value: T): Outcome<T>
        data class Failure<T>(val failure: release.github.Failure): Outcome<T>
    }

    data class GitHubApiAuthority(val authority: Authority) {
        companion object {
            val productionGitHubApi = GitHubApiAuthority(authority(registeredName("api.github.com")))
        }
    }

    data class GitHubUploadAuthority(val authority: Authority) {
        companion object {
            val productionGitHubUpload = GitHubUploadAuthority(authority(registeredName("uploads.github.com")))
        }
    }

    data class GitHubToken(val token: String)

    sealed interface AuditEvent {
        data class RequestCompleted(val uri: URI, val statusCode: Int, val headers: List<Pair<String, String>>, val responseBody: String) : AuditEvent
        data class RequestFailed(val uri: URI, val cause: Throwable) : AuditEvent
    }

}