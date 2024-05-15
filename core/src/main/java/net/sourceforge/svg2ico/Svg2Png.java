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

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;

import static net.sourceforge.svg2ico.SourceImage.sourceImage;

public final class Svg2Png {

    public static void main(String[] args) throws IOException, ImageConversionException {
        try (
                FileInputStream srcFileInputStream = new FileInputStream(args[1]);
                FileOutputStream destFileOutputStream = new FileOutputStream(args[0])
        ) {
            svgToPng(srcFileInputStream, destFileOutputStream, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        }
    }

    private Svg2Png() {
    }

    public static void svgToPng(final InputStream inputStream, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(inputStream, width, height));
    }

    public static void svgToPng(final Reader reader, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(reader, width, height));
    }

    public static void svgToPng(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(inputStream, width, height, colourDepth));
    }

    public static void svgToPng(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(reader, width, height, colourDepth));
    }

    public static void svgToPng(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(inputStream, width, height, userStylesheet));
    }

    public static void svgToPng(final Reader reader, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(reader, width, height, userStylesheet));
    }

    public static void svgToPng(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(inputStream, width, height, colourDepth, userStylesheet));
    }

    public static void svgToPng(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToPng(outputStream, sourceImage(reader, width, height, colourDepth, userStylesheet));
    }

    public static void svgToPng(final OutputStream outputStream, final SourceImage sourceImage) throws IOException, ImageConversionException {
        final BufferedImage bufferedImage = sourceImage.toBufferedImage();
        try {
            new PNGTranscoder().writeImage(bufferedImage, new TranscoderOutput(outputStream));
        } catch (TranscoderException e) {
            throw new ImageConversionException(e);
        }
    }

}
