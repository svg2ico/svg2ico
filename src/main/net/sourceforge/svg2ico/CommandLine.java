/*
 * Copyright 2012 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.sourceforge.svg2ico;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class CommandLine {

    private static final Options OPTIONS = new Options()
            .addOption("src", true, "source SVG file")
            .addOption("dest", true, "destination ICO file")
            .addOption("width", true, "width of output ICO in pixels")
            .addOption("height", true, "height of output ICO in pixels");

    public static void main(String[] args) throws ParseException {
        org.apache.commons.cli.CommandLine commandLine = new PosixParser().parse(OPTIONS, args);
        commandLine.hasOption("src");
    }
}
