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

import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;

import static java.util.Arrays.asList;
import static org.apache.batik.util.XMLResourceDescriptor.setCSSParserClassName;

public final class Svg2Ico {

    private Svg2Ico() {
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(inputStream), width, height), outputStream);
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(reader), width, height), outputStream);
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(inputStream), width, height), colourDepth, outputStream);
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(reader), width, height), colourDepth, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(inputStream), width, height)), new int[]{-1}, new boolean[]{true}, outputStream);
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(reader), width, height)), new int[]{-1}, new boolean[]{true}, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(inputStream), width, height)), new int[]{colourDepth}, new boolean[]{true}, outputStream);
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(reader), width, height)), new int[]{colourDepth}, new boolean[]{true}, outputStream);
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet), outputStream);
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet), outputStream);
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet), colourDepth, outputStream);
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet), colourDepth, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet)), new int[]{-1}, new boolean[]{true}, outputStream);
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet)), new int[]{-1}, new boolean[]{true}, outputStream);
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet)), new int[]{colourDepth}, new boolean[]{true}, outputStream);
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        ICOEncoder.write(asList(loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet)), new int[]{colourDepth}, new boolean[]{true}, outputStream);
    }

    private static BufferedImage loadBufferedImage(TranscoderInput transcoderInput, final float width, final float height) throws FileNotFoundException, ImageConversionException {
        setCSSParserClassName(Parser.class.getCanonicalName());  // To help ShadowJar; if this isn't specified, Batik looks up the fully qualified class name in an XML file.
        return loadImage(transcoderInput, width, height);
    }

    private static BufferedImage loadBufferedImage(TranscoderInput transcoderInput, final float width, final float height, final URI userStylesheet) throws FileNotFoundException, ImageConversionException {
        setCSSParserClassName(Parser.class.getCanonicalName());  // To help ShadowJar; if this isn't specified, Batik looks up the fully qualified class name in an XML file.
        return loadImage(transcoderInput, width, height, userStylesheet);
    }

    private static BufferedImage loadImage(TranscoderInput transcoderInput, final float width, final float height) throws FileNotFoundException, ImageConversionException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

        return loadImage(transcoderInput, imageTranscoder);
    }

    private static BufferedImage loadImage(TranscoderInput transcoderInput, final float width, final float height, final URI userStylesheet) throws FileNotFoundException, ImageConversionException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_USER_STYLESHEET_URI, userStylesheet.toASCIIString());

        return loadImage(transcoderInput, imageTranscoder);
    }

    private static BufferedImage loadImage(TranscoderInput transcoderInput, BufferedImageTranscoder imageTranscoder) throws ImageConversionException {
        try {
            imageTranscoder.transcode(transcoderInput, null);
        } catch (TranscoderException e) {
            throw new ImageConversionException(e);
        }

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

        BufferedImage getBufferedImage() {
            return img;
        }

        @Override
        protected ImageRenderer createRenderer() {
            ImageRenderer r = super.createRenderer();

            RenderingHints rh = r.getRenderingHints();

            rh.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
                    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
            rh.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC));

            rh.add(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON));

            rh.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,
                    RenderingHints.VALUE_COLOR_RENDER_QUALITY));
            rh.add(new RenderingHints(RenderingHints.KEY_DITHERING,
                    RenderingHints.VALUE_DITHER_DISABLE));

            rh.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY));

            rh.add(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE));

            rh.add(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON));
            rh.add(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF));

            r.setRenderingHints(rh);

            return r;
        }
    }
}
