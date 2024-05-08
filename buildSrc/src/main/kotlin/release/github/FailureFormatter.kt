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

import java.io.PrintWriter
import java.io.StringWriter

fun formatFailure(failure: Failure) = when (failure) {
    is Failure.InvalidResponseCode -> StringWriter().also {
        PrintWriter(it).use { printWriter ->
            printWriter.println("Expected request to ${failure.uri} to respond with status code ${failure.expectedResponseCode} but got ${failure.responseCode}")
            printWriter.println("response headers:")
            failure.responseHeaders.forEach { (key, value) ->
                printWriter.println("\t$key: $value")
            }
            printWriter.println("response body:")
            printWriter.println(failure.responseBody)
        }
    }.toString()
    is Failure.RequestSubmittingException -> "Failed submitting request to ${failure.uri} with ${failure.exception}"
    is Failure.ResponseHandlingException -> StringWriter().also {
        PrintWriter(it).use { printWriter ->
            printWriter.println("Request to ${failure.uri} caused ${failure.exception}")
            printWriter.println("response status code: ${failure.responseCode}")
            printWriter.println("response headers:")
            failure.responseHeaders.forEach { (key, value) ->
                printWriter.println("\t$key: $value")
            }
            printWriter.println("response body:")
            printWriter.println(failure.responseBody)
        }
    }.toString()
    is Failure.ConnectTimeout -> "Request to ${failure.uri} exceeded connect timeout of ${failure.connectTimeout}"
    is Failure.FirstByteTimeout -> "Request to ${failure.uri} exceeded first byte timeout of ${failure.firstByteTimeout}"
    is Failure.EndToEndTimeout -> "Request to ${failure.uri} exceeded end to end timeout of ${failure.endToEndTimeout}"
}