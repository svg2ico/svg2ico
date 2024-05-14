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
    alias(libs.plugins.nexusPublish)
    id("release")
}

group = "net.sourceforge.svg2ico"

dependencies {
    releaseJar(project(":svg2ico", "shadow"))
    releaseUserGuide(project(":svg2ico", "userGuide"))
}

tasks {
    val release by registering {
        group = "publishing"
        dependsOn("build", "publish", closeAndReleaseStagingRepositories, sourceforgeRelease, gitHubRelease)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(project.findProperty("ossrhUser").toString())
            password.set(project.findProperty("ossrhPassword").toString())
        }
    }
}