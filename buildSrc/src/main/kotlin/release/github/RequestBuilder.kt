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
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class RequestBuilder private constructor(private val httpRequestBuilder: HttpRequest.Builder) {
    companion object {
        fun get(uri: URI, firstByteTimeout: Duration) = RequestBuilder(baseHttpRequestBuilder(uri, firstByteTimeout).GET())
        fun post(uri: URI, firstByteTimeout: Duration, bodyPublisher: BodyPublisher) = RequestBuilder(baseHttpRequestBuilder(uri, firstByteTimeout).POST(bodyPublisher))
        private fun baseHttpRequestBuilder(uri: URI, firstByteTimeout: Duration) = HttpRequest.newBuilder(uri).setHeader("accept", "application/vnd.github+json")
            .setHeader("accept", "application/vnd.github+json")
            .setHeader("x-github-api-version", "2022-11-28")
            .setHeader("user-agent", "svg2ico-build")
            .timeout(firstByteTimeout.toJavaDuration())
    }

    fun withContentType(contentType: String) = RequestBuilder(httpRequestBuilder.setHeader("content-type", contentType))
    fun withAuthorization(gitHubToken: GitHubHttp.GitHubToken) = RequestBuilder(httpRequestBuilder.setHeader("authorization", "Bearer $gitHubToken"))
    fun build(): HttpRequest = httpRequestBuilder.build()
}