/*
 * Copyright 2018 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.sourceforge.svg2ico;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Svg2IcoPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().create("svg2ico", Svg2IcoTask.class, (task) -> task.getLogger().warn("Can set some stuff on the task now, if we want"));
    }
}
