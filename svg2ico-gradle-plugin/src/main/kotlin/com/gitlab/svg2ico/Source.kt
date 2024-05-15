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

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import javax.inject.Inject

abstract class Source @Inject constructor(private val objectFactory: ObjectFactory) {

    @get:InputFile
    abstract val sourcePath: RegularFileProperty

    @get:InputFile
    @get:Optional
    abstract val userStyleSheet: RegularFileProperty

    @get:Nested
    val outputDimensions: ListProperty<OutputDimension> = objectFactory.listProperty(OutputDimension::class.java)
        .convention(listOf(64, 48, 32, 24, 16).map { dimension ->
            objectFactory.newInstance(OutputDimension::class.java).apply {
                width.set(dimension)
                height.set(dimension)
            }
        })

    @Suppress("unused")
    fun output(action: Action<OutputDimension>) {
        outputDimensions.add(objectFactory.newInstance(OutputDimension::class.java).apply {
            action.execute(this)
        })
    }
}