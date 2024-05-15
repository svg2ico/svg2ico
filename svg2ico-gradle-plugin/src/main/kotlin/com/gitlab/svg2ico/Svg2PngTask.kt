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

import net.sourceforge.svg2ico.ImageConversionException
import net.sourceforge.svg2ico.Svg2Png
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Input
import java.io.IOException

abstract class Svg2PngTask : DefaultTask() {

    @get:InputFile
    abstract val source: RegularFileProperty

    @get:InputFile
    @get:Optional
    abstract val userStyleSheet: RegularFileProperty

    @get:Input
    abstract val width: Property<Int>

    @get:Input
    abstract val height: Property<Int>

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    fun apply() {
        try {
            destination.get().asFile.outputStream().use { destinationOutputStream ->
                source.get().asFile.inputStream().use { sourceInputStream ->
                    userStyleSheet.map {
                        {
                            Svg2Png.svgToPng(
                                sourceInputStream,
                                destinationOutputStream,
                                width.get().toFloat(),
                                height.get().toFloat(),
                                it.asFile.toURI()
                            )
                        }
                    }.getOrElse {
                        Svg2Png.svgToPng(
                            sourceInputStream,
                            destinationOutputStream,
                            width.get().toFloat(),
                            height.get().toFloat(),
                        )
                    }()
                }
            }
        } catch (e: ImageConversionException) {
            throw TaskExecutionException(this, e)
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }
    }

}