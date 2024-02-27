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
    id("com.github.spotbugs") version "6.0.7"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("com.gitlab.svg2ico") version "1.0"
    id("org.asciidoctor.jvm.convert") version "4.0.2"

    id("release.sourceforge")
}

repositories {
    mavenCentral()
}

group = "net.sourceforge.svg2ico"
base.archivesName = "svg2ico"
description = "svg2ico converts images in SVG format to ICO."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    implementation(group = "org.apache.ant", name = "ant", version = "1.10.14")
    implementation(group = "org.apache.xmlgraphics", name = "batik-rasterizer", version = "1.17")
    implementation(group = "commons-cli", name = "commons-cli", version = "1.6.0")
    implementation(group = "org.jclarion", name = "image4j", version = "0.7")

    spotbugs(group = "com.github.spotbugs", name = "spotbugs", version = "4.8.3")
}

tasks {
    jar {
        from(sourceSets["main"].output)
        manifest {
            attributes["Main-Class"] = "net.sourceforge.svg2ico.CommandLine"
        }
    }

    shadowJar {
        relocate("net.sf.image4j", "net.sourceforge.svg2ico.shadowjar.net.sf.image4j")
        relocate("org.apache.batik", "net.sourceforge.svg2ico.shadowjar.org.apache.batik")
        relocate("org.apache.xmlgraphics", "net.sourceforge.svg2ico.shadowjar.org.apache.xmlgraphics")
        relocate("org.w3c.dom.svg", "net.sourceforge.svg2ico.shadowjar.org.w3c.dom.svg")
        relocate("org.apache.commons.cli", "net.sourceforge.svg2ico.shadowjar.org.apache.commons.cli")
        relocate("org.w3c.css", "org.w3c.css")
        relocate("org.w3c.dom", "org.w3c.dom")
        exclude("**/org/w3c/dom/xpath/**/*")
        exclude("**/org/w3c/dom/events/**/*")
        archiveClassifier.set("")
    }

    javadoc {
        title = "svg2ico version $version"
    }

    val ico by registering(com.gitlab.svg2ico.Svg2IcoTask::class) {
        source {
            sourcePath = file("resources/favicon.svg")
        }
        destination = project.layout.buildDirectory.file("icons/favicon.ico")
    }

    val png by registering(com.gitlab.svg2ico.Svg2PngTask::class) {
        source = file("resources/favicon.svg")
        width = 128
        height = 128
        destination = project.layout.buildDirectory.file("icons/favicon.png")
    }

    asciidoctor {
        dependsOn(ico, png) // doesn't seem to infer dependencies properly from the resources CopySpec
        resources {
            from(ico, png)
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier = "javadoc"
    from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier = "sources"
    from(sourceSets["main"].allSource)
}

artifacts {
    archives(javadocJar)
    archives(sourcesJar)
}

val documentationTar by tasks.registering(Tar::class) {
    from(tasks.asciidoctor)
    archiveBaseName.set("documentation")
    compression = Compression.GZIP
}

release {
    jar = tasks.shadowJar.get().archiveFile
    javadocJar = tasks.named<Jar>("javadocJar").get().archiveFile
    documentationTar = tasks.named<Tar>("documentationTar").get().archiveFile
}

tasks.named("release") {
    dependsOn("jar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(sourcesJar)
            artifact(javadocJar)
            from(components["java"])
            pom {
                name = "svg2ico"
                description = project.description
                url = "http://svg2ico.sourceforge.net"
                scm {
                    url = "git://git.code.sf.net/p/svg2ico/git"
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
    sign(publishing.publications["mavenJava"])
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(project.findProperty("ossrhUser").toString())
            password.set(project.findProperty("ossrhPassword").toString())
        }
    }
}

val performRelease by tasks.registering {
    dependsOn(tasks.clean, tasks.build, "publishToSonatype", tasks.closeAndReleaseStagingRepository, tasks.release)
}

tasks.incrementVersionNumber {
    dependsOn(performRelease)
}

tasks.register("deploy") {
    dependsOn(tasks.incrementVersionNumber)
}