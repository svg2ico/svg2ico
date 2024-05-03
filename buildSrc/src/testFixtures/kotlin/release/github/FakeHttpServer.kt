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

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsServer
import net.sourceforge.urin.Authority
import net.sourceforge.urin.Authority.authority
import net.sourceforge.urin.Host.LOCAL_HOST
import net.sourceforge.urin.Port.port
import java.net.InetSocketAddress
import java.security.SecureRandom
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext

class FakeHttpServer private constructor(private val httpServer: HttpServer) : AutoCloseable {
    companion object {
        fun fakeHttpServer(keyManagers: List<KeyManager>, httpHandler: HttpHandler): FakeHttpServer {
            return FakeHttpServer(HttpsServer.create(InetSocketAddress(0), 0).apply {
                httpsConfigurator = HttpsConfigurator(SSLContext.getInstance("TLS").apply {
                    init(keyManagers.toTypedArray(), null, SecureRandom())
                })
                createContext("/", httpHandler)
                start()
            })
        }
    }

    val authority: Authority = authority(LOCAL_HOST, port(httpServer.address.port))

    override fun close() {
        httpServer.stop(0)
    }
}