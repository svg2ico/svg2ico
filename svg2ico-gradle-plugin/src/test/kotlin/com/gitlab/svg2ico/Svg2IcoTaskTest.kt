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
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.TestFactory
import kotlin.io.path.Path
import kotlin.io.path.writeText

class Svg2IcoTaskTest {

    @TestFactory
    fun `Applies dimensions convention to source when no dimensions are specified`() =
        KotlinGroovyTestCase(
            kotlin = """
            plugins {
                id("com.gitlab.svg2ico")
            }
            
            tasks.register("ico", com.gitlab.svg2ico.Svg2IcoTask::class) {
                source {
                    sourcePath = file("resources/favicon.svg")
                }
                destination = project.layout.buildDirectory.file("icons/favicon.ico")
            }
        """,
            groovy = """
            plugins {
                id 'com.gitlab.svg2ico'
            }
            
            task ico (type : Svg2IcoTask) {
                source {
                    sourcePath = file('resources/favicon.svg')
                }
                destination = project.layout.buildDirectory.file('icons/favicon.ico')
            }
        """
        ).test {
            val svgInput = writeSampleSvg(it.resolve(Path("resources")), "favicon.svg")

            val result: BuildResult = GradleRunner.create()
                .withProjectDir(it.toFile())
                .withArguments("ico", "--info")
                .withPluginClasspath()
                .build()

            result.output
                .shouldContain ("Including 64 x 64 image from $svgInput")
                .shouldContain ("Including 48 x 48 image from $svgInput")
                .shouldContain ("Including 32 x 32 image from $svgInput")
                .shouldContain ("Including 24 x 24 image from $svgInput")
                .shouldContain ("Including 16 x 16 image from $svgInput")
            result.task(":ico").shouldNotBeNull().outcome shouldBe SUCCESS

            it.resolve("build/icons/favicon.ico").toFile()
                .shouldBeAFile()
        }

    @TestFactory
    fun `Does not apply convention to source when dimensions are specified`() =
        KotlinGroovyTestCase(
            kotlin = """
            plugins {
                id("com.gitlab.svg2ico")
            }
            
            tasks.register("ico", com.gitlab.svg2ico.Svg2IcoTask::class) {
                source {
                    sourcePath = file("resources/favicon.svg")
                    output { width = 128; height = 128 }
                }
                destination = project.layout.buildDirectory.file("icons/favicon.ico")
            }
        """,
            groovy = """
            plugins {
                id 'com.gitlab.svg2ico'
            }
            
            task ico (type : Svg2IcoTask) {
                source {
                    sourcePath = file('resources/favicon.svg')
                    output { width = 128; height = 128 }
                }
                destination = project.layout.buildDirectory.file('icons/favicon.ico')
            }
        """
        ).test {
            val svgInput = writeSampleSvg(it.resolve(Path("resources")), "favicon.svg")

            val result: BuildResult = GradleRunner.create()
                .withProjectDir(it.toFile())
                .withArguments("ico", "--info")
                .withPluginClasspath()
                .build()

            result.output
                .shouldContain ("Including 128 x 128 image from $svgInput")
                .shouldNotContain ("Including 64 x 64 image from $svgInput")
                .shouldNotContain ("Including 48 x 48 image from $svgInput")
                .shouldNotContain ("Including 32 x 32 image from $svgInput")
                .shouldNotContain ("Including 24 x 24 image from $svgInput")
                .shouldNotContain ("Including 16 x 16 image from $svgInput")
            result.task(":ico").shouldNotBeNull().outcome shouldBe SUCCESS

            it.resolve("build/icons/favicon.ico").toFile()
                .shouldBeAFile()
        }

    @TestFactory
    fun `Works with multiple sources`() =
        KotlinGroovyTestCase(
            kotlin = """
            plugins {
                id("com.gitlab.svg2ico")
            }
            
            tasks.register("ico", com.gitlab.svg2ico.Svg2IcoTask::class) {
                source {
                    sourcePath = file("resources/detailed-favicon.svg")
                    output { width = 64; height = 64 }
                }
                source {
                    sourcePath = file("resources/favicon.svg")
                    output { width = 32; height = 32 }
                }
                destination = project.layout.buildDirectory.file("icons/favicon.ico")
            }
        """,
            groovy = """
            plugins {
                id 'com.gitlab.svg2ico'
            }
            
            task ico (type : Svg2IcoTask) {
                source {
                    sourcePath = file('resources/detailed-favicon.svg')
                    output { width = 64; height = 64 }
                }
                source {
                    sourcePath = file('resources/favicon.svg')
                    output { width = 32; height = 32 }
                }
                destination = project.layout.buildDirectory.file('icons/favicon.ico')
            }
        """
        ).test {
            val resourcesDirectory = it.resolve(Path("resources"))
            val detailedSvgInput = writeSampleSvg(resourcesDirectory, "detailed-favicon.svg")
            val svgInput = writeSampleSvg(resourcesDirectory, "favicon.svg")

            val result: BuildResult = GradleRunner.create()
                .withProjectDir(it.toFile())
                .withArguments("ico", "--info")
                .withPluginClasspath()
                .build()

            result.output
                .shouldContain ("Including 64 x 64 image from $detailedSvgInput")
                .shouldContain ("Including 32 x 32 image from $svgInput")
            result.task(":ico").shouldNotBeNull().outcome shouldBe SUCCESS

            it.resolve("build/icons/favicon.ico").toFile()
                .shouldBeAFile()
        }

    @TestFactory
    fun `Can apply user stylesheet`() =
        KotlinGroovyTestCase(
            kotlin = """
            plugins {
                id("com.gitlab.svg2ico")
            }
            
            tasks.register("ico", com.gitlab.svg2ico.Svg2IcoTask::class) {
                source {
                    sourcePath = file("resources/favicon.svg")
                    userStyleSheet = file("resources/user.css")
                }
                destination = project.layout.buildDirectory.file("icons/favicon.ico")
            }
        """,
            groovy = """
            plugins {
                id 'com.gitlab.svg2ico'
            }
            
            task ico (type : Svg2IcoTask) {
                source {
                    sourcePath = file('resources/favicon.svg')
                    userStyleSheet = file('resources/user.css')
                }
                destination = project.layout.buildDirectory.file('icons/favicon.ico')
            }
        """
        ).test {
            val resourcesDirectory = it.resolve(Path("resources"))
            val svgInput = writeSampleSvg(resourcesDirectory, "favicon.svg")
            resourcesDirectory.apply { toFile().mkdir() }.resolve("user.css").writeText("/* this is actually empty */")


            val result: BuildResult = GradleRunner.create()
                .withProjectDir(it.toFile())
                .withArguments("ico", "--info")
                .withPluginClasspath()
                .build()

            result.output
                .shouldContain ("Including 64 x 64 image from $svgInput")
                .shouldContain ("Including 48 x 48 image from $svgInput")
                .shouldContain ("Including 32 x 32 image from $svgInput")
                .shouldContain ("Including 24 x 24 image from $svgInput")
                .shouldContain ("Including 16 x 16 image from $svgInput")
            result.task(":ico").shouldNotBeNull().outcome shouldBe SUCCESS

            it.resolve("build/icons/favicon.ico").toFile()
                .shouldBeAFile()
        }

}
