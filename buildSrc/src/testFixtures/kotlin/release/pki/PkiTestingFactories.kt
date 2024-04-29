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

import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle.CN
import org.bouncycastle.asn1.x509.Extension.subjectAlternativeName
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralName.dNSName
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger.ZERO
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory


class PkiTestingFactories {
    companion object {

        private val keyStorePassword = "ignored".toCharArray()

        fun aPublicKeyInfrastructure(): PublicKeyInfrastructure {
            val webServerKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
            val now = Instant.now()

            val x509CertificateHolder = X509v3CertificateBuilder(
                X500NameBuilder().addRDN(CN, "My CA").build(),
                ZERO, // serial number
                Date.from(now),
                Date.from(now.plus(Duration.ofDays(1))),
                X500NameBuilder().addRDN(CN, "Your subject").build(),
                SubjectPublicKeyInfo.getInstance(webServerKeyPair.public.encoded)
            ).addExtension(
                subjectAlternativeName, false, DERSequence(
                    arrayOf<ASN1Encodable>(GeneralName(dNSName, "localhost"))
                )
            ).build(JcaContentSignerBuilder("SHA256withRSA").build(webServerKeyPair.private))

            return PublicKeyInfrastructure(
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
                    init(
                        KeyStore.getInstance("PKCS12").apply {
                            load(null)
                            setKeyEntry(
                                "web server",
                                webServerKeyPair.private,
                                keyStorePassword,
                                arrayOf(JcaX509CertificateConverter().getCertificate(x509CertificateHolder))
                            )
                        },
                        keyStorePassword
                    )
                }.keyManagers.asList(),
                ReleaseTrustStore(
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                        .apply {
                            init(KeyStore.getInstance("PKCS12").apply {
                                load(null)
                                setCertificateEntry(
                                    "trust",
                                    JcaX509CertificateConverter().getCertificate(x509CertificateHolder)
                                )
                            })
                        }
                        .trustManagers
                        .filterIsInstance<TrustManager>()
                ))
        }
    }

    data class PublicKeyInfrastructure(val keyManagers: List<KeyManager>, val releaseTrustStore: ReleaseTrustStore)
}