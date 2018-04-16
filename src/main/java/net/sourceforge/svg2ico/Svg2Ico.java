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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static net.sourceforge.svg2ico.SourceImage.sourceImage;
import static net.sourceforge.svg2ico.SourceImage.sourceImageToCompress;

public final class Svg2Ico {

    private Svg2Ico() {
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(inputStream, width, height));
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(reader, width, height));
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(inputStream, width, height, colourDepth));
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(reader, width, height, colourDepth));
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(inputStream, width, height));
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(reader, width, height));
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(inputStream, width, height, colourDepth));
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(reader, width, height, colourDepth));
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(inputStream, width, height, userStylesheet));
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(reader, width, height, userStylesheet));
    }

    public static void svgToIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(inputStream, width, height, colourDepth, userStylesheet));
    }

    public static void svgToIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImage(reader, width, height, colourDepth, userStylesheet));
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(inputStream, width, height, userStylesheet));
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(reader, width, height, userStylesheet));
    }

    public static void svgToCompressedIco(final InputStream inputStream, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(inputStream, width, height, colourDepth, userStylesheet));
    }

    public static void svgToCompressedIco(final Reader reader, final OutputStream outputStream, final float width, final float height, int colourDepth, final URI userStylesheet) throws IOException, ImageConversionException {
        svgToIco(outputStream, sourceImageToCompress(reader, width, height, colourDepth, userStylesheet));
    }

    public static void svgToIco(final OutputStream outputStream, final SourceImage... sourceImages) throws IOException, ImageConversionException {
        svgToIco(outputStream, asList(sourceImages));
    }

    public static void svgToIco(final OutputStream outputStream, final List<SourceImage> sourceImages) throws IOException, ImageConversionException {
        final List<BufferedImage> bufferedImages = new ArrayList<>(sourceImages.size());
        int[] colourDepth = new int[sourceImages.size()];
        boolean[] compress = new boolean[sourceImages.size()];
        int i = 0;
        for (final SourceImage sourceImage : sourceImages) {
            bufferedImages.add(sourceImage.toBufferedImage());
            colourDepth[i] = sourceImage.colourDepth();
            compress[i] = sourceImage.compress();
            i++;
        }
        ICOEncoder.write(bufferedImages, colourDepth, compress, outputStream);
    }

}
