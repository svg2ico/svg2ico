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

import org.gradle.api.logging.Logger
import java.io.PrintWriter
import java.io.StringWriter

class LoggingAuditor(private val logger: Logger) : Auditor<GitHubHttp.AuditEvent> {
    override fun event(auditEvent: GitHubHttp.AuditEvent) = when (auditEvent) {
        is GitHubHttp.AuditEvent.RequestCompleted -> logger.debug(StringWriter().also {
            PrintWriter(it).use { printWriter ->
                printWriter.println("Completed request to ${auditEvent.uri}")
                printWriter.println("response status code: ${auditEvent.statusCode}")
                printWriter.println("response headers:")
                auditEvent.headers.forEach { (key, value) ->
                    printWriter.println("\t$key: $value")
                }
                printWriter.println("response body:")
                printWriter.println(auditEvent.responseBody)
            }
        }.toString())
        is GitHubHttp.AuditEvent.RequestFailed -> logger.debug("Failed request to ${auditEvent.uri} with exception", auditEvent.cause)
    }
}