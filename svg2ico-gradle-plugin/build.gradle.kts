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
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
    alias(libs.plugins.gradlePluginPublish)
}

repositories {
    mavenCentral()
}

group = "com.gitlab.svg2ico"

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    implementation(project(":svg2ico"))
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

@Suppress("UnstableApiUsage")
gradlePlugin {
    website.set("https://svg2ico.sourceforge.net")
    vcsUrl.set("https://github.com/svg2ico/svg2ico.git")
    plugins {
        create("svg2IcoPlugin") {
            id = "com.gitlab.svg2ico"
            implementationClass = "com.gitlab.svg2ico.Svg2IcoPlugin"
            displayName = "svg2ico plugin"
            description = "Converts SVG images into ICO and PNG"
            tags.set(listOf("svg", "ico", "png"))
        }
    }
}