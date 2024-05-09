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

import argo.InvalidSyntaxException
import io.kotest.assertions.assertSoftly
import io.kotest.inspectors.forAll
import io.kotest.inspectors.forOne
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldExistInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import net.sourceforge.urin.Authority
import net.sourceforge.urin.Authority.authority
import net.sourceforge.urin.Host.registeredName
import net.sourceforge.urin.Path.path
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameter
import net.sourceforge.urin.scheme.http.HttpQuery.queryParameters
import net.sourceforge.urin.scheme.http.Https.https
import org.junit.jupiter.api.*
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import release.VersionNumber
import release.github.ConnectionRefusingServer.Companion.connectionRefusingServer
import release.github.FakeHttpServer.Companion.fakeHttpServer
import release.github.GitHub.ReleaseVersionOutcome
import release.github.GitHubHttp.AuditEvent.RequestCompleted
import release.github.GitHubHttp.AuditEvent.RequestFailed
import release.github.GitHubHttp.GitHubApiAuthority
import release.github.GitHubHttp.GitHubUploadAuthority
import release.github.PrivilegedGitHub.*
import release.pki.PkiTestingFactories.Companion.aPublicKeyInfrastructure
import release.pki.ReleaseTrustStore
import release.pki.ReleaseTrustStore.Companion.defaultReleaseTrustStore
import release.utilities.withTemporaryFile
import java.io.IOException
import java.net.URI
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpTimeoutException
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class GitHubHttpTest {

    companion object {
        @Suppress("SpellCheckingInspection")
        const val SAMPLE_VALID_GET_RELEASES_RESPONSE_BODY =
            """[{"url":"https://api.github.com/repos/svg2ico/svg2ico/releases/152871162","assets_url":"https://api.github.com/repos/svg2ico/svg2ico/releases/152871162/assets","upload_url":"https://uploads.github.com/repos/svg2ico/svg2ico/releases/152871162/assets{?name,label}","html_url":"https://github.com/svg2ico/svg2ico/releases/tag/1.82","id":152871162,"author":{"login":"markslater","id":642523,"node_id":"MDQ6VXNlcjY0MjUyMw==","avatar_url":"https://avatars.githubusercontent.com/u/642523?v=4","gravatar_id":"","url":"https://api.github.com/users/markslater","html_url":"https://github.com/markslater","followers_url":"https://api.github.com/users/markslater/followers","following_url":"https://api.github.com/users/markslater/following{/other_user}","gists_url":"https://api.github.com/users/markslater/gists{/gist_id}","starred_url":"https://api.github.com/users/markslater/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/markslater/subscriptions","organizations_url":"https://api.github.com/users/markslater/orgs","repos_url":"https://api.github.com/users/markslater/repos","events_url":"https://api.github.com/users/markslater/events{/privacy}","received_events_url":"https://api.github.com/users/markslater/received_events","type":"User","site_admin":false},"node_id":"RE_kwDOLuWgG84JHKD6","tag_name":"1.82","target_commitish":"master","name":null,"draft":false,"prerelease":false,"created_at":"2024-04-25T19:15:30Z","published_at":"2024-04-25T19:17:40Z","assets":[{"url":"https://api.github.com/repos/svg2ico/svg2ico/releases/assets/164247312","id":164247312,"node_id":"RA_kwDOLuWgG84JyjcQ","name":"svg2ico-1.82.jar","label":"Jar","uploader":{"login":"markslater","id":642523,"node_id":"MDQ6VXNlcjY0MjUyMw==","avatar_url":"https://avatars.githubusercontent.com/u/642523?v=4","gravatar_id":"","url":"https://api.github.com/users/markslater","html_url":"https://github.com/markslater","followers_url":"https://api.github.com/users/markslater/followers","following_url":"https://api.github.com/users/markslater/following{/other_user}","gists_url":"https://api.github.com/users/markslater/gists{/gist_id}","starred_url":"https://api.github.com/users/markslater/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/markslater/subscriptions","organizations_url":"https://api.github.com/users/markslater/orgs","repos_url":"https://api.github.com/users/markslater/repos","events_url":"https://api.github.com/users/markslater/events{/privacy}","received_events_url":"https://api.github.com/users/markslater/received_events","type":"User","site_admin":false},"content_type":"application/java-archive","state":"uploaded","size":7275600,"download_count":4,"created_at":"2024-04-25T19:17:41Z","updated_at":"2024-04-25T19:17:42Z","browser_download_url":"https://github.com/svg2ico/svg2ico/releases/download/1.82/svg2ico-1.82.jar"}],"tarball_url":"https://api.github.com/repos/svg2ico/svg2ico/tarball/1.82","zipball_url":"https://api.github.com/repos/svg2ico/svg2ico/zipball/1.82","body":null}]"""
        @Suppress("SpellCheckingInspection")
        const val SAMPLE_VALID_CREATE_RELEASE_RESPONSE_BODY = """{"url":"https://api.github.com/repos/svg2ico/svg2ico/releases/152871162","assets_url":"https://api.github.com/repos/svg2ico/svg2ico/releases/152871162/assets","upload_url":"https://uploads.github.com/repos/svg2ico/svg2ico/releases/152871162/assets{?name,label}","html_url":"https://github.com/svg2ico/svg2ico/releases/tag/1.82","id":152871162,"author":{"login":"markslater","id":642523,"node_id":"MDQ6VXNlcjY0MjUyMw==","avatar_url":"https://avatars.githubusercontent.com/u/642523?v=4","gravatar_id":"","url":"https://api.github.com/users/markslater","html_url":"https://github.com/markslater","followers_url":"https://api.github.com/users/markslater/followers","following_url":"https://api.github.com/users/markslater/following{/other_user}","gists_url":"https://api.github.com/users/markslater/gists{/gist_id}","starred_url":"https://api.github.com/users/markslater/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/markslater/subscriptions","organizations_url":"https://api.github.com/users/markslater/orgs","repos_url":"https://api.github.com/users/markslater/repos","events_url":"https://api.github.com/users/markslater/events{/privacy}","received_events_url":"https://api.github.com/users/markslater/received_events","type":"User","site_admin":false},"node_id":"RE_kwDOLuWgG84JHKD6","tag_name":"1.82","target_commitish":"master","name":null,"draft":false,"prerelease":false,"created_at":"2024-04-25T19:15:30Z","published_at":"2024-04-25T19:17:40Z","assets":[],"tarball_url":"https://api.github.com/repos/svg2ico/svg2ico/tarball/1.82","zipball_url":"https://api.github.com/repos/svg2ico/svg2ico/zipball/1.82","body":null}"""
        @Suppress("SpellCheckingInspection")
        const val SAMPLE_VALID_UPLOAD_ARTIFACT_RESPONSE_BODY = """{"url":"https://api.github.com/repos/svg2ico/svg2ico/releases/assets/164247312","id":164247312,"node_id":"RA_kwDOLuWgG84JyjcQ","name":"svg2ico-1.82.jar","label":"Jar","uploader":{"login":"markslater","id":642523,"node_id":"MDQ6VXNlcjY0MjUyMw==","avatar_url":"https://avatars.githubusercontent.com/u/642523?v=4","gravatar_id":"","url":"https://api.github.com/users/markslater","html_url":"https://github.com/markslater","followers_url":"https://api.github.com/users/markslater/followers","following_url":"https://api.github.com/users/markslater/following{/other_user}","gists_url":"https://api.github.com/users/markslater/gists{/gist_id}","starred_url":"https://api.github.com/users/markslater/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/markslater/subscriptions","organizations_url":"https://api.github.com/users/markslater/orgs","repos_url":"https://api.github.com/users/markslater/repos","events_url":"https://api.github.com/users/markslater/events{/privacy}","received_events_url":"https://api.github.com/users/markslater/received_events","type":"User","site_admin":false},"content_type":"application/java-archive","state":"uploaded","size":7275600,"download_count":0,"created_at":"2024-04-25T19:17:41Z","updated_at":"2024-04-25T19:17:42Z","browser_download_url":"https://github.com/svg2ico/svg2ico/releases/download/1.82/svg2ico-1.82.jar"}"""
    }

    private val publicKeyInfrastructure = aPublicKeyInfrastructure()

    @TestFactory
    fun `test suites`(): List<DynamicNode> {
        return listOf(
            object : TestSuiteParameters<ReleaseVersionOutcome>("latest release version") {
                override val executor = { apiAuthority: GitHubApiAuthority, _: GitHubUploadAuthority, auditor: Auditor<GitHubHttp.AuditEvent> ->
                    GitHubHttp(
                        apiAuthority,
                        publicKeyInfrastructure.releaseTrustStore,
                        auditor
                    ).latestReleaseVersion()
                }
                override val validResponseCode = 200
                override val sunnyDayResponse = SAMPLE_VALID_GET_RELEASES_RESPONSE_BODY
                override val sunnyDayAssertion: (result: ReleaseVersionOutcome) -> Unit = { it.shouldBeInstanceOf<ReleaseVersionOutcome.Success>().versionNumber shouldBe VersionNumber.ReleaseVersion.of(1, 82) }
                override val expectedUri: (apiAuthority: Authority, uploadAuthority: Authority) -> URI = { apiAuthority, _ ->
                    https(
                        apiAuthority,
                        path("repos", "svg2ico", "svg2ico", "releases"),
                        queryParameters(queryParameter("per_page", "1"))
                    ).asUri()
                }
            },
            object : TestSuiteParameters<ReleaseOutcome>("create release") {
                override val executor = { apiAuthority: GitHubApiAuthority, uploadAuthority: GitHubUploadAuthority, auditor: Auditor<GitHubHttp.AuditEvent> ->
                    GitHubHttp(
                        apiAuthority,
                        publicKeyInfrastructure.releaseTrustStore,
                        auditor
                    ).privileged(uploadAuthority, GitHubHttp.GitHubToken("Test me"))
                        .release(VersionNumber.ReleaseVersion.of(1, 82)) // TODO test token and version are handled correctly
                }
                override val validResponseCode = 201
                override val sunnyDayResponse = SAMPLE_VALID_CREATE_RELEASE_RESPONSE_BODY
                override val sunnyDayAssertion: (result: ReleaseOutcome) -> Unit = { it.shouldBeInstanceOf<ReleaseOutcome.Success>().releaseId shouldBe ReleaseId("152871162") }
                override val expectedUri: (apiAuthority: Authority, uploadAuthority: Authority) -> URI = { apiAuthority, _ ->
                    https(
                        apiAuthority,
                        path("repos", "svg2ico", "svg2ico", "releases")
                    ).asUri()
                }
            },
            object : TestSuiteParameters<UploadArtifactOutcome>("upload artifact") {
                private val releaseId = "152871162"
                private val versionNumber = VersionNumber.ReleaseVersion.of(1, 82)
                private val fileContents = byteArrayOf(0x0f, 0x0d, 0x7a)
                override val executor = { apiAuthority: GitHubApiAuthority, uploadAuthority: GitHubUploadAuthority, auditor: Auditor<GitHubHttp.AuditEvent> ->
                    withTemporaryFile(fileContents) { file ->
                        GitHubHttp(
                            apiAuthority,
                            publicKeyInfrastructure.releaseTrustStore,
                            auditor
                        ).privileged(uploadAuthority, GitHubHttp.GitHubToken("Test me")).uploadArtifact(
                            versionNumber,
                            ReleaseId(releaseId),
                            file
                        ) // TODO test token and file contents are handled correctly
                    }
                }
                override val validResponseCode = 201
                override val sunnyDayResponse = SAMPLE_VALID_UPLOAD_ARTIFACT_RESPONSE_BODY
                override val sunnyDayAssertion: (result: UploadArtifactOutcome) -> Unit = { it.shouldBeInstanceOf<UploadArtifactOutcome.Success>() }
                override val expectedUri: (apiAuthority: Authority, uploadAuthority: Authority) -> URI = { _, uploadAuthority ->
                    https(
                        uploadAuthority,
                        path("repos", "svg2ico", "svg2ico", "releases", releaseId, "assets"),
                        queryParameters(
                            queryParameter("name", "svg2ico-${versionNumber}.jar"),
                            queryParameter("label", "Jar"),
                        )
                    ).asUri()
                }
            },
        ).map { it.toDynamicNode() }
    }

    private abstract inner class TestSuiteParameters<OUTCOME>(val name: String) {
        abstract val executor: (GitHubApiAuthority, GitHubUploadAuthority, Auditor<GitHubHttp.AuditEvent>) -> OUTCOME
        abstract val validResponseCode: Int
        abstract val sunnyDayResponse: String
        abstract val sunnyDayAssertion: (result: OUTCOME) -> Unit
        abstract val expectedUri: (apiAuthority: Authority, uploadAuthority: Authority) -> URI

        private fun sunnyDay(): DynamicTest = dynamicTest("sunny day") {
            val responseBodyBytes = sunnyDayResponse.toByteArray(UTF_8)
            fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
                exchange.sendResponseHeaders(validResponseCode, responseBodyBytes.size.toLong())
                exchange.responseBody.use { it.write(responseBodyBytes) }
            }.use { fakeGitHubServer ->
                val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
                val releaseVersionOutcome = executor(GitHubApiAuthority(fakeGitHubServer.authority), GitHubUploadAuthority(fakeGitHubServer.authority), recordingAuditor) // TODO do we need separate test servers for api and upload
                sunnyDayAssertion(releaseVersionOutcome)
                recordingAuditor.auditEvents().shouldBeSingleton().forAll { element ->
                    element.shouldBeInstanceOf<RequestCompleted>().also {
                        assertSoftly(element) {
                            it.uri shouldBe expectedUri(fakeGitHubServer.authority, fakeGitHubServer.authority)
                            it.statusCode shouldBe validResponseCode
                            it.headers.shouldContain("content-length" to responseBodyBytes.size.toString())
                            it.responseBody shouldBe responseBody
                        }
                    }
                }
            }
        }

        private fun setsRequestHeaders(): DynamicTest = dynamicTest("sets request headers") {
            val responseBodyBytes = sunnyDayResponse.toByteArray(UTF_8)
            val receivedRequestHeaders = mutableListOf<Pair<String, String>>()
            fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
                exchange.requestHeaders.forEach { entry ->
                    entry.value.forEach { value ->
                        receivedRequestHeaders.add(entry.key to value)
                    }
                }
                exchange.sendResponseHeaders(validResponseCode, responseBodyBytes.size.toLong())
                exchange.responseBody.use { it.write(responseBodyBytes) }
            }.use { fakeGitHubServer ->
                executor(GitHubApiAuthority(fakeGitHubServer.authority), GitHubUploadAuthority(fakeGitHubServer.authority)) {}
                receivedRequestHeaders
                    .forOne { (key, value) ->
                        key shouldBeEqualIgnoringCase "x-github-api-version"
                        value shouldBe "2022-11-28"
                    }
                    .forOne { (key, value) ->
                        key shouldBeEqualIgnoringCase "accept"
                        value shouldBe "application/vnd.github+json"
                    }
                    .forOne { (key, value) ->
                        key shouldBeEqualIgnoringCase "user-agent"
                        value shouldBe "svg2ico-build"
                    }
            }
        }

        fun toDynamicNode() : DynamicNode = dynamicContainer(name, listOf(
            sunnyDay(), setsRequestHeaders()
        ))
    }

    @Test
    fun `handles unexpectedly-shaped json response`() {
        val responseCode = 200
        val responseBody = """{}"""
        val responseBodyBytes = responseBody.toByteArray(UTF_8)
        fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
            exchange.sendResponseHeaders(responseCode, responseBodyBytes.size.toLong())
            exchange.responseBody.use { it.write(responseBodyBytes) }
        }.use { fakeGitHubServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            val releaseVersionOutcome = GitHubHttp(GitHubApiAuthority(fakeGitHubServer.authority), publicKeyInfrastructure.releaseTrustStore, recordingAuditor)
                .latestReleaseVersion()
            val expectedRequestUri =
                https(fakeGitHubServer.authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
            releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.ResponseHandlingException>()
                .also { failure ->
                    assertSoftly(failure) {
                        it.uri shouldBe expectedRequestUri
                        it.responseCode shouldBe responseCode
                        it.responseHeaders.shouldContain("content-length" to responseBodyBytes.size.toString())
                        it.responseBody shouldBe responseBody
                        it.exception.shouldBeInstanceOf<IllegalArgumentException>()
                    }
                }
            recordingAuditor.auditEvents().shouldBeSingleton().forAll { element ->
                element.shouldBeInstanceOf<RequestCompleted>().also {
                    assertSoftly(element) {
                        it.uri shouldBe expectedRequestUri
                        it.statusCode shouldBe responseCode
                        it.headers.shouldContain("content-length" to responseBodyBytes.size.toString())
                        it.responseBody shouldBe responseBody
                    }
                }
            }
        }
    }


    @Test
    fun `handles non-json response`() {
        val responseCode = 200
        val responseBody = """not json"""
        val responseBodyBytes = responseBody.toByteArray(UTF_8)
        fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
            exchange.sendResponseHeaders(responseCode, responseBodyBytes.size.toLong())
            exchange.responseBody.use { it.write(responseBodyBytes) }
        }.use { fakeGitHubServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            val releaseVersionOutcome = GitHubHttp(GitHubApiAuthority(fakeGitHubServer.authority), publicKeyInfrastructure.releaseTrustStore, recordingAuditor)
                .latestReleaseVersion()
            val expectedRequestUri =
                https(fakeGitHubServer.authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
            releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.ResponseHandlingException>()
                .also { failure ->
                    assertSoftly(failure) {
                        it.uri shouldBe expectedRequestUri
                        it.responseCode shouldBe responseCode
                        it.responseHeaders.shouldContain("content-length" to responseBodyBytes.size.toString())
                        it.responseBody shouldBe responseBody
                        it.exception.shouldBeInstanceOf<InvalidSyntaxException>()
                    }
                }
            recordingAuditor.auditEvents().shouldBeSingleton().forAll { element ->
                element.shouldBeInstanceOf<RequestCompleted>().also {
                    assertSoftly(element) {
                        it.uri shouldBe expectedRequestUri
                        it.statusCode shouldBe responseCode
                        it.headers.shouldContain("content-length" to responseBodyBytes.size.toString())
                        it.responseBody shouldBe responseBody
                    }
                }
            }
        }
    }


    @Test
    fun `handles unexpected response code`() {
        val responseCode = 403
        val responseBody = """"you're not allowed to see this""""
        val responseBodyBytes = responseBody.toByteArray(UTF_8)
        fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
            exchange.sendResponseHeaders(responseCode, responseBodyBytes.size.toLong())
            exchange.responseBody.use { it.write(responseBodyBytes) }
        }.use { fakeGitHubServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            val releaseVersionOutcome = GitHubHttp(
                GitHubApiAuthority(fakeGitHubServer.authority),
                publicKeyInfrastructure.releaseTrustStore,
                recordingAuditor,
                connectTimeout = 1.seconds
            )
                .latestReleaseVersion()
            val expectedRequestUri =
                https(fakeGitHubServer.authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
            releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.InvalidResponseCode>()
                .also { failure ->
                    assertSoftly(failure) {
                        it.uri shouldBe expectedRequestUri
                        it.expectedResponseCode shouldBe 200
                        it.responseCode shouldBe responseCode
                        it.responseHeaders.shouldContain("content-length" to responseBodyBytes.size.toString())
                        it.responseBody shouldBe responseBody
                    }
                }
            recordingAuditor.auditEvents().shouldBeSingleton().forAll { element ->
                element.shouldBeInstanceOf<RequestCompleted>().also {
                    assertSoftly(element) {
                        it.uri shouldBe expectedRequestUri
                        it.statusCode shouldBe responseCode
                        it.headers.shouldContain("content-length" to responseBodyBytes.size.toString())
                        it.responseBody shouldBe responseBody
                    }
                }
            }
        }
    }


    @Test
    fun `handles IOException processing response`() {
        val responseBody = """"something short""""
        val responseCode = 200
        fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
            exchange.sendResponseHeaders(responseCode, 1024)
            exchange.responseBody.use { it.write(responseBody.toByteArray(UTF_8)) }
        }.use { fakeGitHubServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            val releaseVersionOutcome = GitHubHttp(GitHubApiAuthority(fakeGitHubServer.authority), publicKeyInfrastructure.releaseTrustStore, recordingAuditor)
                .latestReleaseVersion()
            val expectedRequestUri =
                https(fakeGitHubServer.authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
            releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.RequestSubmittingException>()
                .also { failure ->
                    assertSoftly(failure) {
                        it.uri shouldBe expectedRequestUri
                        it.exception.shouldBeInstanceOf<IOException>()
                    }
                }
            recordingAuditor.auditEvents().shouldExistInOrder(
                { it is RequestFailed && it.uri == expectedRequestUri && it.cause is IOException }
            )
        }
    }


    @Test
    fun `handles unresolvable address`() {
        val authority = authority(registeredName("something.invalid"))
        val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
        val releaseVersionOutcome = GitHubHttp(GitHubApiAuthority(authority), defaultReleaseTrustStore, recordingAuditor).latestReleaseVersion()
        val expectedRequestUri =
            https(authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
        releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.RequestSubmittingException>()
            .also { failure ->
                assertSoftly(failure) {
                    it.uri shouldBe expectedRequestUri
                    it.exception.shouldBeInstanceOf<IOException>()
                }
            }
        recordingAuditor.auditEvents().shouldExistInOrder(
            { it is RequestFailed && it.uri == expectedRequestUri && it.cause is IOException }
        )
    }


    @Test
    fun `handles ssl failure`() {
        val responseCode = 200
        fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
            val responseBytes = SAMPLE_VALID_GET_RELEASES_RESPONSE_BODY.toByteArray(UTF_8)
            exchange.sendResponseHeaders(responseCode, responseBytes.size.toLong())
            exchange.responseBody.use { it.write(responseBytes) }
        }.use { fakeGitHubServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            val releaseVersionOutcome = GitHubHttp(GitHubApiAuthority(fakeGitHubServer.authority), ReleaseTrustStore(emptyList()), recordingAuditor)
                .latestReleaseVersion()
            val expectedRequestUri =
                https(fakeGitHubServer.authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
            releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.RequestSubmittingException>()
                .also { failure ->
                    assertSoftly(failure) {
                        it.uri shouldBe expectedRequestUri
                        it.exception.shouldBeInstanceOf<IOException>()
                    }
                }
            recordingAuditor.auditEvents().shouldExistInOrder(
                { it is RequestFailed && it.uri == expectedRequestUri && it.cause is IOException }
            )
        }
    }


    @Test
    @Timeout(value = 2, unit = SECONDS)
    fun `handles connect timeout`() {
        connectionRefusingServer(publicKeyInfrastructure).use { connectionRefusingServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            val connectTimeout = 100.milliseconds
            val releaseVersionOutcome = GitHubHttp(
                GitHubApiAuthority(connectionRefusingServer.authority),
                publicKeyInfrastructure.releaseTrustStore,
                recordingAuditor,
                connectTimeout = connectTimeout,
                firstByteTimeout = 10.seconds,
                endToEndTimeout = 10.seconds,
            ).latestReleaseVersion()
            val expectedRequestUri =
                https(
                    connectionRefusingServer.authority,
                    path("repos", "svg2ico", "svg2ico", "releases"),
                    queryParameters(queryParameter("per_page", "1"))
                ).asUri()
            releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.ConnectTimeout>()
                .also { failure ->
                    assertSoftly(failure) {
                        it.uri shouldBe expectedRequestUri
                        it.connectTimeout shouldBe connectTimeout
                        it.exception.shouldBeInstanceOf<HttpConnectTimeoutException>()
                    }
                }
            recordingAuditor.auditEvents().shouldExistInOrder(
                { it is RequestFailed && it.uri == expectedRequestUri && it.cause is HttpConnectTimeoutException }
            )
        }
    }


    @Test
    @Timeout(value = 2, unit = SECONDS)
    fun `handles timeout awaiting first response byte`() {
        val responseCode = 200
        val countDownLatch = CountDownLatch(1)
        fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
            countDownLatch.await()
            exchange.sendResponseHeaders(responseCode, 2)
            exchange.responseBody.use { it.write("[]".toByteArray(UTF_8)) }
        }.use { fakeGitHubServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            try {
                val firstByteTimeout = 100.milliseconds
                val releaseVersionOutcome = GitHubHttp(
                    GitHubApiAuthority(fakeGitHubServer.authority),
                    publicKeyInfrastructure.releaseTrustStore,
                    recordingAuditor,
                    connectTimeout = 10.seconds,
                    firstByteTimeout = firstByteTimeout,
                    endToEndTimeout = 10.seconds
                )
                    .latestReleaseVersion()
                val expectedRequestUri =
                    https(fakeGitHubServer.authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
                releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.FirstByteTimeout>()
                    .also { failure ->
                        assertSoftly(failure) {
                            it.uri shouldBe expectedRequestUri
                            it.firstByteTimeout shouldBe firstByteTimeout
                            it.exception.shouldBeInstanceOf<HttpTimeoutException>().shouldNotBeInstanceOf<HttpConnectTimeoutException>()
                        }
                    }
                recordingAuditor.auditEvents().shouldExistInOrder(
                    { it is RequestFailed && it.uri == expectedRequestUri && it.cause is HttpTimeoutException && it.cause !is HttpConnectTimeoutException }
                )
            } finally {
                countDownLatch.countDown()
            }
        }
    }


    @Test
    @Timeout(value = 2, unit = SECONDS)
    fun `handles timeout during slow response`() {
        val responseCode = 200
        val countDownLatch = CountDownLatch(1)
        fakeHttpServer(publicKeyInfrastructure.keyManagers) { exchange ->
            exchange.sendResponseHeaders(responseCode, 23)
            exchange.responseBody.use {
                it.write("\"first part".toByteArray(UTF_8))
                countDownLatch.await()
                it.write("second part/".toByteArray(UTF_8))
            }
        }.use { fakeGitHubServer ->
            val recordingAuditor = RecordingAuditor<GitHubHttp.AuditEvent>()
            try {
                val endToEndTimeout = 100.milliseconds
                val releaseVersionOutcome = GitHubHttp(
                    GitHubApiAuthority(fakeGitHubServer.authority),
                    publicKeyInfrastructure.releaseTrustStore,
                    recordingAuditor,
                    connectTimeout = 10.seconds,
                    firstByteTimeout = 10.seconds,
                    endToEndTimeout = endToEndTimeout
                )
                    .latestReleaseVersion()
                val expectedRequestUri =
                    https(fakeGitHubServer.authority, path("repos", "svg2ico", "svg2ico", "releases"), queryParameters(queryParameter("per_page", "1"))).asUri()
                releaseVersionOutcome.shouldBeInstanceOf<ReleaseVersionOutcome.Failure>().failure.shouldBeInstanceOf<Failure.EndToEndTimeout>()
                    .also { failure ->
                        assertSoftly(failure) {
                            it.uri shouldBe expectedRequestUri
                            it.endToEndTimeout shouldBe endToEndTimeout
                            it.exception.shouldBeInstanceOf<TimeoutException>()
                        }
                    }
                recordingAuditor.auditEvents().shouldExistInOrder(
                    { it is RequestFailed && it.uri == expectedRequestUri && it.cause is TimeoutException }
                )
            } finally {
                countDownLatch.countDown()
            }
        }
    }

}