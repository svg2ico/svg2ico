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

import org.apache.batik.transcoder.TranscoderException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static net.sourceforge.svg2ico.Svg2Ico.svgToIco;

public final class Svg2IcoTask extends Task {

    private File dest;
    private File src;

    public void execute() {
        if (dest == null) {
            throw new BuildException("Mandatory dest not set.");
        }
        if (src == null) {
            throw new BuildException("Mandatory src not set.");
        }
        try {
            svgToIco(new FileInputStream(src), new FileOutputStream(dest));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TranscoderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void setDest(final File dest) {
        this.dest = dest;
    }

    public void setSrc(final File src) {
        this.src = src;
    }

}
