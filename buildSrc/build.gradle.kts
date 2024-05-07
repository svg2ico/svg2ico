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
    `java-test-fixtures`
    `kotlin-dsl`
    alias(libs.plugins.kotlinJvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.kotlinCoroutines)
    implementation(libs.maverickSynergyClient)
    implementation(libs.argo)
    implementation(libs.urin)

    testFixturesImplementation(libs.bouncycastleProvider)
    testFixturesImplementation(libs.bouncycastlePkix)
}

testing {
    @Suppress("UnstableApiUsage")
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit)
            dependencies {
                implementation(libs.kotest)
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