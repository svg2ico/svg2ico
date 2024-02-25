/*
 * Copyright 2024 Mark Slater
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

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static net.sourceforge.svg2ico.SourceImage.sourceImage;
import static net.sourceforge.svg2ico.SourceImage.sourceImageToCompress;
import static net.sourceforge.svg2ico.Svg2Ico.svgToIco;

public final class Svg2IcoTask extends Task {

    private File dest;
    private File src;
    private File userStylesheet;
    private Float width;
    private Float height;
    private Integer depth;
    private Boolean compress;
    private final List<SourceImage> sourceImages = new LinkedList<>();

    public void execute() {
        if (sourceImages.isEmpty() && !(isSet(src) && isSet(width) && isSet(height))) {
            throw new BuildException("Must set src, width and height attributes or supply at least one sourceImage nested element.");
        } else {
            if (isSet(src) && isSet(width) && isSet(height)) {
                sourceImages.add(0, new SourceImage(src, userStylesheet, width, height, depth, compress));
            }
            try (FileOutputStream outputStream = new FileOutputStream(checkSet("dest", dest))) {
                final List<Closeable> stuffToClose = new ArrayList<>(sourceImages.size());
                final List<net.sourceforge.svg2ico.SourceImage> apiSourceImages = new ArrayList<>(sourceImages.size());
                try {
                    for (SourceImage sourceImage : sourceImages) {
                        final FileInputStream inputStream = new FileInputStream(checkSet("src", sourceImage.src));
                        stuffToClose.add(inputStream);
                        final float width = checkSet("width", sourceImage.width);
                        final float height = checkSet("height", sourceImage.height);
                        final net.sourceforge.svg2ico.SourceImage apiSourceImage;
                        if (isSet(sourceImage.depth)) {
                            if (isSet(sourceImage.userStylesheet)) {
                                if (isSet(sourceImage.compress) && sourceImage.compress) {
                                    apiSourceImage = sourceImageToCompress(inputStream, width, height, sourceImage.depth, sourceImage.userStylesheet.toURI());
                                } else {
                                    apiSourceImage = sourceImage(inputStream, width, height, sourceImage.depth, sourceImage.userStylesheet.toURI());
                                }
                            } else {
                                if (isSet(sourceImage.compress) && sourceImage.compress) {
                                    apiSourceImage = sourceImageToCompress(inputStream, width, height, sourceImage.depth);
                                } else {
                                    apiSourceImage = sourceImage(inputStream, width, height, sourceImage.depth);
                                }
                            }
                        } else {
                            if (isSet(sourceImage.userStylesheet)) {
                                if (isSet(sourceImage.compress) && sourceImage.compress) {
                                    apiSourceImage = sourceImageToCompress(inputStream, width, height, sourceImage.userStylesheet.toURI());
                                } else {
                                    apiSourceImage = sourceImage(inputStream, width, height, sourceImage.userStylesheet.toURI());
                                }
                            } else {
                                if (isSet(sourceImage.compress) && sourceImage.compress) {
                                    apiSourceImage = sourceImageToCompress(inputStream, width, height);
                                } else {
                                    apiSourceImage = sourceImage(inputStream, width, height);
                                }
                            }
                        }
                        apiSourceImages.add(apiSourceImage);
                    }
                    svgToIco(outputStream, apiSourceImages);
                } finally {
                    for (Closeable closeable : stuffToClose) {
                        closeable.close();
                    }
                }
            } catch (IOException | ImageConversionException e) {
                throw new BuildException("Failed converting SVG " + src + " to ICO " + dest + ".", e);
            }
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
        return value != null;
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

    public void addConfiguredSourceImage(SourceImage sourceImage) {
        sourceImages.add(sourceImage);
    }

    public static final class SourceImage {
        private File src;
        private File userStylesheet;
        private Float width;
        private Float height;
        private Integer depth;
        private Boolean compress;

        public SourceImage() {
        }

        public SourceImage(File src, File userStylesheet, Float width, Float height, Integer depth, Boolean compress) {
            this.src = src;
            this.userStylesheet = userStylesheet;
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.compress = compress;
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
}
