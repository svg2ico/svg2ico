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

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

class SourceforgeReleasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.version = Properties().apply {
            target.layout.projectDirectory.file("version.properties").asFile.reader().use {
                load(it)
            }
        }.let {
            "${it.getProperty("majorVersion")}.${it.getProperty("minorVersion")}"
        }
        val extension = target.extensions.create("release", SourceforgeReleasePluginExtension::class.java)
        target.tasks.register("release", SourceforgeReleaseTask::class.java) {
            jar.set(extension.jar)
            documentationTar.set(extension.documentationTar)
        }
        target.tasks.register("incrementVersionNumber", IncrementVersionNumberTask::class.java)
    }
}