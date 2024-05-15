/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.gitlab.svg2ico

import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.TestFactory
import kotlin.io.path.Path
import kotlin.io.path.writeText

class Svg2PngTaskTest {

    @TestFactory
    fun `Can make a png`() =
        KotlinGroovyTestCase(
            kotlin = """
            plugins {
                id("com.gitlab.svg2ico")
            }
            
            tasks.register("png", com.gitlab.svg2ico.Svg2PngTask::class) {
                source = file("resources/favicon.svg")
                width = 128
                height = 128
                destination = project.layout.buildDirectory.file("icons/favicon.png")
            }
        """,
            groovy = """
            plugins {
                id 'com.gitlab.svg2ico'
            }
            
            task png (type : Svg2PngTask) {
                source = file('resources/favicon.svg')
                width = 128
                height = 128
                destination = project.layout.buildDirectory.file('icons/favicon.png')
            }
        """
        ).test {
            writeSampleSvg(it.resolve(Path("resources")), "favicon.svg")

            val result: BuildResult = GradleRunner.create()
                .withProjectDir(it.toFile())
                .withArguments("png")
                .withPluginClasspath()
                .build()

            result.task(":png").shouldNotBeNull().outcome shouldBe TaskOutcome.SUCCESS

            it.resolve("build/icons/favicon.png").toFile()
                .shouldBeAFile()
        }

    @TestFactory
    fun `Can apply user stylesheet`() =
        KotlinGroovyTestCase(
            kotlin = """
            plugins {
                id("com.gitlab.svg2ico")
            }
            
            tasks.register("png", com.gitlab.svg2ico.Svg2PngTask::class) {
                source = file("resources/favicon.svg")
                width = 128
                height = 128
                userStyleSheet = file("resources/user.css")
                destination = project.layout.buildDirectory.file("icons/favicon.png")
            }
        """,
            groovy = """
            plugins {
                id 'com.gitlab.svg2ico'
            }
            
            task png (type : Svg2PngTask) {
                source = file('resources/favicon.svg')
                width = 128
                height = 128
                userStyleSheet = file('resources/user.css')
                destination = project.layout.buildDirectory.file('icons/favicon.png')
            }
        """
        ).test {
            val resourcesDirectory = it.resolve(Path("resources"))
            writeSampleSvg(resourcesDirectory, "favicon.svg")
            resourcesDirectory.apply { toFile().mkdir() }.resolve("user.css").writeText("/* this is actually empty */")


            val result: BuildResult = GradleRunner.create()
                .withProjectDir(it.toFile())
                .withArguments("png")
                .withPluginClasspath()
                .build()

            result.task(":png").shouldNotBeNull().outcome shouldBe TaskOutcome.SUCCESS

            it.resolve("build/icons/favicon.png").toFile()
                .shouldBeAFile()
        }

}