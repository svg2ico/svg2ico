/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package release.pki

import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory


class ReleaseTrustStore(private val trustManagers: List<TrustManager>) {
    companion object {
        val defaultReleaseTrustStore: ReleaseTrustStore
            get() = ReleaseTrustStore(
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                    .apply { init(null as KeyStore?) }
                    .trustManagers
                    .filterIsInstance<TrustManager>()
            )
    }

    val sslContext: SSLContext
        get() {
            SSLContext.getDefault()
            return SSLContext.getInstance("TLS").apply {
                init(emptyArray(), trustManagers.toTypedArray(), SecureRandom())
            }
        }
}