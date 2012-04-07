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

import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.image.BufferedImage;
import java.io.*;

import static org.apache.batik.util.XMLResourceDescriptor.setCSSParserClassName;

public class Svg2IcoCore {
    static void svgToIco(final FileInputStream inputStream, final OutputStream outputStream) throws TranscoderException, IOException {
        setCSSParserClassName(Parser.class.getCanonicalName());  // To help JarJar; if this isn't specified, Batik looks up the fully qualified class name in an XML file.
        BufferedImage bufferedImage = loadImage(32, 32, inputStream);
        ICOEncoder.write(bufferedImage, outputStream);
    }

    static BufferedImage loadImage(float width, float height, final InputStream inputStream) throws TranscoderException, FileNotFoundException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

        TranscoderInput input = new TranscoderInput(inputStream);
        imageTranscoder.transcode(input, null);

        return imageTranscoder.getBufferedImage();
    }

    static final class BufferedImageTranscoder extends ImageTranscoder {
        private BufferedImage img = null;

        @Override
        public BufferedImage createImage(int w, int h) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(BufferedImage img, TranscoderOutput output) {
            this.img = img;
        }

        public BufferedImage getBufferedImage() {
            return img;
        }
    }
}
