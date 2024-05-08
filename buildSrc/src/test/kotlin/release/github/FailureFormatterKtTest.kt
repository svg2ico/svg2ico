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

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpConnectTimeoutException
import java.net.http.HttpTimeoutException
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.seconds

class FailureFormatterKtTest {

    @Test
    fun `formats InvalidResponseCode`() {
        formatFailure(
            Failure.InvalidResponseCode(
                URI("http://example.com"),
                404,
                200,
                listOf("date" to "Wed, 21 Oct 2015 07:28:00 GMT"),
                "Not found"
            )
        ) shouldBe """Expected request to http://example.com to respond with status code 200 but got 404
response headers:
	date: Wed, 21 Oct 2015 07:28:00 GMT
response body:
Not found
"""
    }

    @Test
    fun `formats RequestSubmittingException`() {
        formatFailure(
            Failure.RequestSubmittingException(
                URI("http://example.com"),
                IllegalArgumentException("whoops")
            )
        ) shouldBe "Failed submitting request to http://example.com with java.lang.IllegalArgumentException: whoops"
    }

    @Test
    fun `formats ResponseHandlingException`() {
        formatFailure(
            Failure.ResponseHandlingException(
                URI("http://example.com"),
                200,
                listOf("date" to "Wed, 21 Oct 2015 07:28:00 GMT"),
                "whoops",
                IllegalArgumentException("whoops")
            )
        ) shouldBe """Request to http://example.com caused java.lang.IllegalArgumentException: whoops
response status code: 200
response headers:
	date: Wed, 21 Oct 2015 07:28:00 GMT
response body:
whoops
"""
    }

    @Test
    fun `formats ConnectTimeoutException`() {
        formatFailure(
            Failure.ConnectTimeout(
                URI("http://example.com"),
                1.seconds,
                HttpConnectTimeoutException("whoops")
            )
        ) shouldBe "Request to http://example.com exceeded connect timeout of 1s"
    }

    @Test
    fun `formats FirstByteTimeoutException`() {
        formatFailure(
            Failure.FirstByteTimeout(
                URI("http://example.com"),
                1.seconds,
                HttpTimeoutException("whoops")
            )
        ) shouldBe "Request to http://example.com exceeded first byte timeout of 1s"
    }

    @Test
    fun `formats EndToEndTimeoutException`() {
        formatFailure(
            Failure.EndToEndTimeout(
                URI("http://example.com"),
                1.seconds,
                TimeoutException("whoops")
            )
        ) shouldBe "Request to http://example.com exceeded end to end timeout of 1s"
    }

}