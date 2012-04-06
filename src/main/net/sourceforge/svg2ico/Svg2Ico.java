package net.sourceforge.svg2ico;

import net.sf.image4j.codec.ico.ICOEncoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Svg2Ico {
    public void execute() {
        try {
            BufferedImage bufferedImage = loadImage(new File("docs/favicon.svg"), 32, 32);
            ICOEncoder.write(bufferedImage, new File("output.ico"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TranscoderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static BufferedImage loadImage(File svgFile, float width, float height) throws TranscoderException, FileNotFoundException {
        BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
        imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height);

        TranscoderInput input = new TranscoderInput(new FileInputStream(svgFile));
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
