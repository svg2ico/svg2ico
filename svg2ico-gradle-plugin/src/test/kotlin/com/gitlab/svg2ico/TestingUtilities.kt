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

import org.junit.jupiter.api.DynamicTest
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

fun <T> inTempDirectory(block: (Path) -> T): T {
    val tempDirectory: Path = createTempDirectory()
    return try {
        block(tempDirectory)
    } finally {
        tempDirectory.toFile().deleteRecursively()
    }
}

fun writeSampleSvg(directory: Path, filename: String): Path = directory.apply { toFile().mkdir() }
    .resolve(filename).apply {
        writeText(
            """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
                    <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
                    <svg xmlns="http://www.w3.org/2000/svg">
                    </svg>""".trimIndent()
        )
    }

data class KotlinGroovyTestCase(private val kotlin: String, private val groovy: String) {
    fun test(block: (Path) -> Unit) = listOf(
        Triple("kotlin", kotlin, "build.gradle.kts"),
        Triple("groovy", groovy, "build.gradle"),
    ).map { (name, buildFile, buildFileName) ->
        DynamicTest.dynamicTest(name) {
            inTempDirectory {
                it.resolve(buildFileName).writeText(buildFile)
                block(it)
            }
        }
    }
}