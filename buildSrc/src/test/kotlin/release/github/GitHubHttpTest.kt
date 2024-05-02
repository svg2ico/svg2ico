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

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsServer
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import net.sourceforge.urin.Authority.authority
import net.sourceforge.urin.Host.LOCAL_HOST
import net.sourceforge.urin.Path.path
import net.sourceforge.urin.Port.port
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameter
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameters
import net.sourceforge.urin.scheme.http.Https.https
import org.junit.jupiter.api.Test
import release.VersionNumber
import release.github.GitHub.ReleaseVersionOutcome
import release.github.GitHubHttp.AuditEvent.RequestCompleted
import release.github.GitHubHttp.GitHubApiAuthority
import release.pki.PkiTestingFactories.Companion.aPublicKeyInfrastructure
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom
import javax.net.ssl.SSLContext

class GitHubHttpTest {
    @Test
    fun `can get latest release version`() {
        val publicKeyInfrastructure = aPublicKeyInfrastructure()
        @Suppress("SpellCheckingInspection")
        val responseBody =
            """[{"url":"https://api.github.com/repos/svg2ico/svg2ico/releases/152871162","assets_url":"https://api.github.com/repos/svg2ico/svg2ico/releases/152871162/assets","upload_url":"https://uploads.github.com/repos/svg2ico/svg2ico/releases/152871162/assets{?name,label}","html_url":"https://github.com/svg2ico/svg2ico/releases/tag/1.82","id":152871162,"author":{"login":"markslater","id":642523,"node_id":"MDQ6VXNlcjY0MjUyMw==","avatar_url":"https://avatars.githubusercontent.com/u/642523?v=4","gravatar_id":"","url":"https://api.github.com/users/markslater","html_url":"https://github.com/markslater","followers_url":"https://api.github.com/users/markslater/followers","following_url":"https://api.github.com/users/markslater/following{/other_user}","gists_url":"https://api.github.com/users/markslater/gists{/gist_id}","starred_url":"https://api.github.com/users/markslater/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/markslater/subscriptions","organizations_url":"https://api.github.com/users/markslater/orgs","repos_url":"https://api.github.com/users/markslater/repos","events_url":"https://api.github.com/users/markslater/events{/privacy}","received_events_url":"https://api.github.com/users/markslater/received_events","type":"User","site_admin":false},"node_id":"RE_kwDOLuWgG84JHKD6","tag_name":"1.82","target_commitish":"master","name":null,"draft":false,"prerelease":false,"created_at":"2024-04-25T19:15:30Z","published_at":"2024-04-25T19:17:40Z","assets":[{"url":"https://api.github.com/repos/svg2ico/svg2ico/releases/assets/164247312","id":164247312,"node_id":"RA_kwDOLuWgG84JyjcQ","name":"svg2ico-1.82.jar","label":"Jar","uploader":{"login":"markslater","id":642523,"node_id":"MDQ6VXNlcjY0MjUyMw==","avatar_url":"https://avatars.githubusercontent.com/u/642523?v=4","gravatar_id":"","url":"https://api.github.com/users/markslater","html_url":"https://github.com/markslater","followers_url":"https://api.github.com/users/markslater/followers","following_url":"https://api.github.com/users/markslater/following{/other_user}","gists_url":"https://api.github.com/users/markslater/gists{/gist_id}","starred_url":"https://api.github.com/users/markslater/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/markslater/subscriptions","organizations_url":"https://api.github.com/users/markslater/orgs","repos_url":"https://api.github.com/users/markslater/repos","events_url":"https://api.github.com/users/markslater/events{/privacy}","received_events_url":"https://api.github.com/users/markslater/received_events","type":"User","site_admin":false},"content_type":"application/java-archive","state":"uploaded","size":7275600,"download_count":4,"created_at":"2024-04-25T19:17:41Z","updated_at":"2024-04-25T19:17:42Z","browser_download_url":"https://github.com/svg2ico/svg2ico/releases/download/1.82/svg2ico-1.82.jar"}],"tarball_url":"https://api.github.com/repos/svg2ico/svg2ico/tarball/1.82","zipball_url":"https://api.github.com/repos/svg2ico/svg2ico/zipball/1.82","body":null}]"""
        val httpsServer = HttpsServer.create(InetSocketAddress(0), 0).apply {
            httpsConfigurator = HttpsConfigurator(SSLContext.getInstance("TLS").apply {
                init(publicKeyInfrastructure.keyManagers.toTypedArray(), null, SecureRandom())
            })
            createContext("/repos/svg2ico/svg2ico/releases") { exchange: HttpExchange ->
                exchange.sendResponseHeaders(200, 0) // TODO maybe need to specify length with shorter response
                exchange.responseBody.use {
                    it.write(
                        responseBody.toByteArray(
                            UTF_8
                        )
                    )
                }
            }
            start()
        }
        val authority = authority(LOCAL_HOST, port(httpsServer.address.port))
        val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
        val releaseVersionOutcome = try {
            GitHubHttp(
                GitHubApiAuthority(authority),
                publicKeyInfrastructure.releaseTrustStore,
                recordingAuditor
            ).latestReleaseVersion()
        } finally {
            httpsServer.stop(0)
        }
        releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Success>().versionNumber shouldBe VersionNumber.ReleaseVersion.of(1, 82)
        recordingAuditor.auditEvents() shouldContainExactly listOf(
            RequestCompleted(
                https(authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri(),
                200,
                responseBody
            )
        )
    }

    @Test
    fun `handles unexpectedly-shaped json response`() {
        val publicKeyInfrastructure = aPublicKeyInfrastructure()
        val responseBody =
            """{}"""
        val httpsServer = HttpsServer.create(InetSocketAddress(0), 0).apply {
            httpsConfigurator = HttpsConfigurator(SSLContext.getInstance("TLS").apply {
                init(publicKeyInfrastructure.keyManagers.toTypedArray(), null, SecureRandom())
            })
            createContext("/repos/svg2ico/svg2ico/releases") { exchange: HttpExchange ->
                exchange.sendResponseHeaders(200, 0) // TODO maybe need to specify length with shorter response
                exchange.responseBody.use {
                    it.write(
                        responseBody.toByteArray(
                            UTF_8
                        )
                    )
                }
            }
            start()
        }
        val authority = authority(LOCAL_HOST, port(httpsServer.address.port))
        val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
        val releaseVersionOutcome = try {
            GitHubHttp(
                GitHubApiAuthority(authority),
                publicKeyInfrastructure.releaseTrustStore,
                recordingAuditor
            ).latestReleaseVersion()
        } finally {
            httpsServer.stop(0)
        }
        releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.ResponseHandlingException>().also { failure ->
            assertSoftly(failure) {
                it.uri shouldBe https(authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
                it.responseCode shouldBe 200
                it.responseBody shouldBe responseBody
                it.exception.shouldBeInstanceOf<IllegalArgumentException>()
            }
        }
        recordingAuditor.auditEvents() shouldContainExactly listOf(
            RequestCompleted(
                https(authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri(),
                200,
                responseBody
            )
        )
    }

}