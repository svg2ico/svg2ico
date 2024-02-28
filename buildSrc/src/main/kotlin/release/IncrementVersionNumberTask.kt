/*
 * Copyright 2024 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package release

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.util.*
import javax.inject.Inject

@UntrackedTask(because = "Reads from and writes to version.properties, so it invalidates itself")
abstract class IncrementVersionNumberTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {

    @get:InputFile
    protected val versionPropertiesFile: RegularFileProperty = objectFactory.fileProperty().value(project.layout.projectDirectory.file("version.properties"))

    @TaskAction
    fun apply() {
        Properties().apply {
            versionPropertiesFile.get().asFile.reader().use {
                load(it)
            }
            setProperty("minorVersion", (getProperty("minorVersion").toInt() + 1).toString())
            versionPropertiesFile.get().asFile.writer().use {
                store(it, null)
            }
        }
    }

}