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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static net.sourceforge.svg2ico.Svg2Ico.svgToCompressedIco;
import static net.sourceforge.svg2ico.Svg2Ico.svgToIco;

public final class Svg2IcoTask extends Task {

    private File dest;
    private File src;
    private File userStylesheet;
    private Float width;
    private Float height;
    private Integer depth;
    private Boolean compress;

    public void execute() {
        try (
                final FileInputStream inputStream = new FileInputStream(checkSet("src", src));
                final FileOutputStream outputStream = new FileOutputStream(checkSet("dest", dest))
        ) {

            final Float width = checkSet("width", this.width);
            final Float height = checkSet("height", this.height);
            if (isSet(depth)) {
                if (isSet(userStylesheet)) {
                    if (isSet(compress) && compress) {
                        svgToCompressedIco(inputStream, outputStream, width, height, depth, userStylesheet.toURI());
                    } else {
                        svgToIco(inputStream, outputStream, width, height, depth, userStylesheet.toURI());
                    }
                } else {
                    if (isSet(compress) && compress) {
                        svgToCompressedIco(inputStream, outputStream, width, height, depth);
                    } else {
                        svgToIco(inputStream, outputStream, width, height, depth);
                    }
                }
            } else {
                if (isSet(userStylesheet)) {
                    if (isSet(compress) && compress) {
                        svgToCompressedIco(inputStream, outputStream, width, height, userStylesheet.toURI());
                    } else {
                        svgToIco(inputStream, outputStream, width, height, userStylesheet.toURI());
                    }
                } else {
                    if (isSet(compress) && compress) {
                        svgToCompressedIco(inputStream, outputStream, width, height);
                    } else {
                        svgToIco(inputStream, outputStream, width, height);
                    }
                }
            }
        } catch (IOException | ImageConversionException e) {
            throw new BuildException("Failed converting SVG " + src + " to ICO " + dest + ".", e);
        }
    }

    private static <T> T checkSet(final String fieldName, final T value) {
        if (value == null) {
            throw new BuildException("Mandatory " + fieldName + " attribute not set.");
        } else {
            return value;
        }
    }

    private static boolean isSet(Object value) {
        return !(value == null);
    }

    public void setDest(final File dest) {
        this.dest = dest;
    }

    public void setSrc(final File src) {
        this.src = src;
    }

    public void setUserStylesheet(final File userStylesheet) {
        this.userStylesheet = userStylesheet;
    }

    public void setWidth(final float width) {
        this.width = width;
    }

    public void setHeight(final float height) {
        this.height = height;
    }

    public void setDepth(final int depth) {
        this.depth = depth;
    }

    public void setCompress(final boolean compress) {
        this.compress = compress;
    }
}
