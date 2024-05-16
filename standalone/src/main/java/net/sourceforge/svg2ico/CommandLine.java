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

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static net.sourceforge.svg2ico.Svg2Ico.svgToCompressedIco;
import static net.sourceforge.svg2ico.Svg2Ico.svgToIco;

public final class CommandLine {

    private static final Options OPTIONS = new Options()
            .addOption("src", true, "source SVG file")
            .addOption("dest", true, "destination ICO file")
            .addOption("width", true, "width of output ICO in pixels")
            .addOption("height", true, "height of output ICO in pixels")
            .addOption("depth", true, "optional colour depth in bits per pixel")
            .addOption("compress", false, "optional flag to output compressed ICO")
            .addOption("userStylesheet", true, "optional user stylesheet file");

    public static void main(String[] args) {
        org.apache.commons.cli.CommandLine commandLine;
        try {
            commandLine = new DefaultParser().parse(OPTIONS, args);
            if (!commandLine.hasOption("src") || !commandLine.hasOption("dest") || !commandLine.hasOption("width") || !commandLine.hasOption("height")) {
                printHelp();
            } else {
                try {
                    File src = new File(commandLine.getOptionValue("src"));
                    File dest = new File(commandLine.getOptionValue("dest"));
                    if (!src.exists()) {
                        System.err.println("src file not found " + src);
                    } else {
                        try (
                                FileInputStream srcFileInputStream = new FileInputStream(src);
                                FileOutputStream destFileOutputStream = new FileOutputStream(dest)
                        ) {
                            float width = parseFloat(commandLine.getOptionValue("width"));
                            float height = parseFloat(commandLine.getOptionValue("height"));
                            if (commandLine.hasOption("depth")) {
                                int depth = parseInt(commandLine.getOptionValue("depth"));
                                if (commandLine.hasOption("userStylesheet")) {
                                    File userStylesheet = new File(commandLine.getOptionValue("userStylesheet"));
                                    if (commandLine.hasOption("compress")) {
                                        svgToCompressedIco(srcFileInputStream, destFileOutputStream, width, height, depth, userStylesheet.toURI());
                                    } else {
                                        svgToIco(srcFileInputStream, destFileOutputStream, width, height, depth, userStylesheet.toURI());
                                    }
                                } else {
                                    if (commandLine.hasOption("compress")) {
                                        svgToCompressedIco(srcFileInputStream, destFileOutputStream, width, height, depth);
                                    } else {
                                        svgToIco(srcFileInputStream, destFileOutputStream, width, height, depth);
                                    }
                                }
                            } else {
                                if (commandLine.hasOption("userStylesheet")) {
                                    File userStylesheet = new File(commandLine.getOptionValue("userStylesheet"));
                                    if (commandLine.hasOption("compress")) {
                                        svgToCompressedIco(srcFileInputStream, destFileOutputStream, width, height, userStylesheet.toURI());
                                    } else {
                                        svgToIco(srcFileInputStream, destFileOutputStream, width, height, userStylesheet.toURI());
                                    }
                                } else {
                                    if (commandLine.hasOption("compress")) {
                                        svgToCompressedIco(srcFileInputStream, destFileOutputStream, width, height);
                                    } else {
                                        svgToIco(srcFileInputStream, destFileOutputStream, width, height);
                                    }
                                }
                            }
                        }
                    }
                } catch (ImageConversionException | IOException e) {
                    printFailure(e);
                }
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp();
        }
    }

    private static void printFailure(final Exception e) {
        System.err.println("failed to create ICO");
        e.printStackTrace();
    }

    private static void printHelp() {
        new HelpFormatter().printHelp("svg2ico", OPTIONS);
    }
}
