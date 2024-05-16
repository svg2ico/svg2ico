/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    java
    signing
    `maven-publish`
    `jvm-test-suite`
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.svg2ico)
    alias(libs.plugins.asciidoctorConvert)
}

repositories {
    mavenCentral()
}

group = "net.sourceforge.svg2ico"
description = "svg2ico converts images in SVG format to ICO."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

val userGuide: Configuration by configurations.creating

dependencies {
    implementation(libs.batikTranscoder)
    implementation(libs.image4j)

    runtimeOnly(libs.batikCodec)

    spotbugs(libs.spotbugs)
}

testing {
    @Suppress("UnstableApiUsage")
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit)
            dependencies {
                implementation(libs.commonsIO)
            }
        }
    }
}

tasks {
    javadoc {
        title = "svg2ico version $version"
    }

    val ico by registering(com.gitlab.svg2ico.Svg2IcoTask::class) {
        group = "documentation"
        source {
            sourcePath = file("resources/favicon.svg")
        }
        destination = project.layout.buildDirectory.file("icons/favicon.ico")
    }

    val png by registering(com.gitlab.svg2ico.Svg2PngTask::class) {
        group = "documentation"
        source = file("resources/favicon.svg")
        width = 500
        height = 500
        destination = project.layout.buildDirectory.file("icons/favicon.png")
    }

    asciidoctor {
        dependsOn(ico, png, javadoc) // doesn't seem to infer dependencies properly from the resources CopySpec
        resources {
            from(ico, png)
            from(javadoc) {
                into("javadoc")
            }
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    group = "documentation"
    archiveClassifier = "javadoc"
    from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
    group = "documentation"
    archiveClassifier = "sources"
    from(sourceSets["main"].allSource)
}

val documentationTar by tasks.registering(Tar::class) {
    group = "documentation"
    from(tasks.asciidoctor)
    archiveBaseName.set("documentation")
    compression = Compression.GZIP
}

artifacts {
    add(userGuide.name, documentationTar) {
        classifier = "userGuide"
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            pom {
                name = "svg2ico"
                description = project.description
                url = "https://svg2ico.sourceforge.net"
                scm {
                    url = "https://github.com/svg2ico/svg2ico.git"
                }
                developers {
                    developer {
                        id = "mos20"
                        name = "Mark Slater"
                    }
                }
                licenses {
                    license {
                        name = "The Apache Software License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}