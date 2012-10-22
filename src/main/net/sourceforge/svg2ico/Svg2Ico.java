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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import static java.util.Arrays.asList;
import static org.apache.batik.util.XMLResourceDescriptor.setCSSParserClassName;

public final class Svg2Ico {

    private Svg2Ico() {
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height) throws TranscoderException, IOException {
        ICOEncoder.write(loadBufferedImage(inputStream, width, height), outputStream);
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth) throws TranscoderException, IOException {
        ICOEncoder.write(loadBufferedImage(inputStream, width, height), colourDepth, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height) throws TranscoderException, IOException {
        ICOEncoder.write(asList(loadBufferedImage(inputStream, width, height)), new int[]{-1}, new boolean[] {true}, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth) throws TranscoderException, IOException {
        ICOEncoder.write(asList(loadBufferedImage(inputStream, width, height)), new int[]{colourDepth}, new boolean[] {true}, outputStream);
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws TranscoderException, IOException {
        ICOEncoder.write(loadBufferedImage(inputStream, width, height, userStylesheet), outputStream);
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws TranscoderException, IOException {
        ICOEncoder.write(loadBufferedImage(inputStream, width, height, userStylesheet), colourDepth, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws TranscoderException, IOException {
        ICOEncoder.write(asList(loadBufferedImage(inputStream, width, height, userStylesheet)), new int[]{-1}, new boolean[] {true}, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws TranscoderException, IOException {
        ICOEncoder.write(asList(loadBufferedImage(inputStream, width, height, userStylesheet)), new int[]{colourDepth}, new boolean[] {true}, outputStream);
    }

    private static BufferedImage loadBufferedImage(final InputStream inputStream, final float width, final float height) throws TranscoderException, FileNotFoundException {
        setCSSParserClassName(Parser.class.getCanonicalName());  // To help JarJar; if this isn't specified, Batik looks up the fully qualified class name in an XML file.
        return loadImage(width, height, inputStream);
    }

    private static BufferedImage loadBufferedImage(final InputStream inputStream, final float width, final float height, final URI userStylesheet) throws TranscoderException, FileNotFoundException {
        setCSSParserClassName(Parser.class.getCanonicalName());  // To help JarJar; if this isn't specified, Batik looks up the fully qualified class name in an XML file.
        return loadImage(width, height, userStylesheet, inputStream);
    }

    private static BufferedImage loadImage(final float width, final float height, final InputStream inputStream) throws TranscoderException, FileNotFoundException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

        return loadImage(inputStream, imageTranscoder);
    }

    private static BufferedImage loadImage(final float width, final float height, final URI userStylesheet, final InputStream inputStream) throws TranscoderException, FileNotFoundException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_USER_STYLESHEET_URI, userStylesheet.toASCIIString());

        return loadImage(inputStream, imageTranscoder);
    }

    private static BufferedImage loadImage(InputStream inputStream, BufferedImageTranscoder imageTranscoder) throws TranscoderException {
        TranscoderInput input = new TranscoderInput(inputStream);
        imageTranscoder.transcode(input, null);

        return imageTranscoder.getBufferedImage();
    }

    private static final class BufferedImageTranscoder extends ImageTranscoder {
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
