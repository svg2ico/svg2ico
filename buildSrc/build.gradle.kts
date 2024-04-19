/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

plugins {
    `jvm-test-suite`
    `kotlin-dsl`
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(group = "com.sshtools", name = "maverick-synergy-client", version = "3.1.1")
    implementation(group = "net.sourceforge.argo", name = "argo", version = "7.3")
    implementation(group = "net.sourceforge.urin", name = "urin", version = "4.9")
}

testing {
    @Suppress("UnstableApiUsage")
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("io.kotest:kotest-assertions-core:5.8.1")
            }
        }
    }
}

gradlePlugin {
    plugins {
        create("releasePlugin") {
            id = "release"
            implementationClass = "release.ReleasePlugin"
        }
    }
}