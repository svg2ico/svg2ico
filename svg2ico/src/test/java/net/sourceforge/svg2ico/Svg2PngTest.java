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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.apache.commons.io.output.NullOutputStream.INSTANCE;

class Svg2PngTest {

    @Test
    void canConvertASampleSvgToIco() throws ImageConversionException, IOException {
        Svg2Png.svgToPng(new StringReader("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"32\" height=\"32\" version=\"1.1\">\n" +
                "    <circle fill=\"#00ff00\" cx=\"16\" cy=\"16\" r=\"8\"/>\n" +
                "</svg>\n"), INSTANCE, 32.0f, 32.0f);
    }

}