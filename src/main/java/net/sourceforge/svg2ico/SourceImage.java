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

import org.apache.batik.css.parser.Parser;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

import static java.lang.Boolean.TRUE;
import static org.apache.batik.util.XMLResourceDescriptor.setCSSParserClassName;

public abstract class SourceImage {

    private SourceImage() {
    }

    public static SourceImage sourceImage(final InputStream inputStream, final float width, final float height) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImage(final Reader reader, final float width, final float height) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImage(final InputStream inputStream, final float width, final float height, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImage(final Reader reader, final float width, final float height, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImage(final InputStream inputStream, final float width, final float height, final int colourDepth) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    public static SourceImage sourceImage(final Reader reader, final float width, final float height, final int colourDepth) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    public static SourceImage sourceImage(final InputStream inputStream, final float width, final float height, final int colourDepth, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    public static SourceImage sourceImage(final Reader reader, final float width, final float height, final int colourDepth, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final InputStream inputStream, final float width, final float height) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return true;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final Reader reader, final float width, final float height) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return true;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final InputStream inputStream, final float width, final float height, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return true;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final Reader reader, final float width, final float height, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return true;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return -1;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final InputStream inputStream, final float width, final float height, final int colourDepth) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return true;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final Reader reader, final float width, final float height, final int colourDepth) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return true;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final InputStream inputStream, final float width, final float height, final int colourDepth, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return true;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(inputStream), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    public static SourceImage sourceImageToCompress(final Reader reader, final float width, final float height, final int colourDepth, final URI userStylesheet) {
        return new SourceImage() {
            @Override
            boolean compress() {
                return false;
            }

            @Override
            BufferedImage toBufferedImage() throws ImageConversionException {
                return SourceImage.loadBufferedImage(new TranscoderInput(reader), width, height, userStylesheet);
            }

            @Override
            int colourDepth() {
                return colourDepth;
            }
        };
    }

    abstract BufferedImage toBufferedImage() throws FileNotFoundException, ImageConversionException;

    abstract int colourDepth();

    abstract boolean compress();

    private static BufferedImage loadBufferedImage(TranscoderInput transcoderInput, final float width, final float height) throws ImageConversionException {
        setCSSParserClassName(Parser.class.getCanonicalName());  // To help ShadowJar; if this isn't specified, Batik looks up the fully qualified class name in an XML file.
        return loadImage(transcoderInput, width, height);
    }

    private static BufferedImage loadBufferedImage(TranscoderInput transcoderInput, final float width, final float height, final URI userStylesheet) throws ImageConversionException {
        setCSSParserClassName(Parser.class.getCanonicalName());  // To help ShadowJar; if this isn't specified, Batik looks up the fully qualified class name in an XML file.
        return loadImage(transcoderInput, width, height, userStylesheet);
    }

    private static BufferedImage loadImage(TranscoderInput transcoderInput, final float width, final float height) throws ImageConversionException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

        return loadImage(transcoderInput, imageTranscoder);
    }

    private static BufferedImage loadImage(TranscoderInput transcoderInput, final float width, final float height, final URI userStylesheet) throws ImageConversionException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_USER_STYLESHEET_URI, userStylesheet.toASCIIString());
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, TRUE);

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
