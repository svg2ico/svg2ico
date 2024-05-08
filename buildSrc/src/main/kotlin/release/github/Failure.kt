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

import java.net.URI
import kotlin.time.Duration

sealed interface Failure {
    data class InvalidResponseCode(
        val uri: URI,
        val responseCode: Int,
        val expectedResponseCode: Int,
        val responseHeaders: List<Pair<String, String>>,
        val responseBody: String
    ) : Failure
    sealed interface ExceptionalFailure : Failure {
        val exception: Throwable
    }
    data class ResponseHandlingException(
        val uri: URI,
        val responseCode: Int,
        val responseHeaders: List<Pair<String, String>>,
        val responseBody: String,
        override val exception: Throwable
    ) : ExceptionalFailure
    data class RequestSubmittingException(val uri: URI, override val exception: Throwable) : ExceptionalFailure
    data class ConnectTimeout(val uri: URI, val connectTimeout: Duration, override val exception: Throwable) : ExceptionalFailure
    data class FirstByteTimeout(val uri: URI, val firstByteTimeout: Duration, override val exception: Throwable) : ExceptionalFailure
    data class EndToEndTimeout(val uri: URI, val endToEndTimeout: Duration, override val exception: Throwable) : ExceptionalFailure
}