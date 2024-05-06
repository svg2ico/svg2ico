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

class FailureFormatterKtTest {

    @Test
    fun `formats InvalidResponseCode`() {
        formatFailure(
            Failure.InvalidResponseCode(
                URI("http://example.com"),
                404,
                200,
                "Not found"
            )
        ) shouldBe "Expected request to http://example.com to respond with status code 200 but got 404 with body Not found"
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
                "whoops",
                IllegalArgumentException("whoops")
            )
        ) shouldBe "Request to http://example.com responded with status code 200 and body whoops which caused java.lang.IllegalArgumentException: whoops"
    }

}