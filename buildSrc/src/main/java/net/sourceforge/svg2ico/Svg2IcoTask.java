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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class Svg2IcoTask extends DefaultTask {

    private String svgFile;
    private String destinationFile;
    private float width;
    private float height;

    public void setSvgFile(String svgFile) {
        this.svgFile = svgFile;
    }

    public void setDestination(String destinationFile) {
        this.destinationFile = destinationFile;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @TaskAction
    void generateIco() {
        final File sourceFile = getProject().file(svgFile);
        getLogger().warn("make me an ico from " + sourceFile);
        final File destination = getProject().file(destinationFile);
        getLogger().warn("and put it in " + destination);
        getLogger().warn("making it " + width + " pixels wide and " + height + " pixels high.");
    }
}
