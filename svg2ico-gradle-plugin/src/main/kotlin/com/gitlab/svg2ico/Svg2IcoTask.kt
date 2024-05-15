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
import net.sourceforge.svg2ico.SourceImage
import net.sourceforge.svg2ico.SourceImage.sourceImage
import net.sourceforge.svg2ico.Svg2Ico
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import java.io.IOException
import javax.inject.Inject

abstract class Svg2IcoTask @Inject constructor(private val objectFactory: ObjectFactory) : DefaultTask() {

    @get:Nested
    protected abstract val sources: ListProperty<Source>

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @Suppress("unused")
    fun source(action: Action<Source>) {
        sources.add(objectFactory.newInstance(Source::class.java).apply {
            action.execute(this)
        })
    }

    @TaskAction
    fun apply() {
        data class Input(val sourcePath: RegularFileProperty, val userStylesheet: RegularFileProperty, val outputDimension: OutputDimension)

        val inputs = sources.get().flatMap { source ->
            source.outputDimensions.get().map { Input(source.sourcePath, source.userStyleSheet, it) }
        }
        try {
            destination.get().asFile.outputStream().use { destinationOutputStream ->
                inputs.foldRight({ sourceImages: List<SourceImage> ->
                    Svg2Ico.svgToIco(destinationOutputStream, sourceImages)
                }) { input, acc ->
                    { sourceImages ->
                        input.sourcePath.get().asFile.inputStream().use { sourceInputStream ->
                            logger.info("Including ${input.outputDimension.width.get()} x ${input.outputDimension.height.get()} image from ${input.sourcePath.get()}")
                            val sourceImage = input.userStylesheet.map {
                                sourceImage(
                                    sourceInputStream,
                                    input.outputDimension.width.get().toFloat(),
                                    input.outputDimension.height.get().toFloat(),
                                    it.asFile.toURI()
                                )
                            }.getOrElse(
                                sourceImage(
                                    sourceInputStream,
                                    input.outputDimension.width.get().toFloat(),
                                    input.outputDimension.height.get().toFloat()
                                )
                            )
                            acc(sourceImages + sourceImage)
                        }
                    }
                }(emptyList())
            }
        } catch (e: ImageConversionException) {
            throw TaskExecutionException(this, e)
        } catch (e: IOException) {
            throw TaskExecutionException(this, e)
        }
    }
}