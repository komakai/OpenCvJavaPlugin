package org.opencv.intellij.plugin.visualizations;

import java.awt.image.BufferedImage;

public class BitmapDecoder {

    public interface BitmapExtractor {
        BufferedImage getImage(int width, int height, byte[] data);
    }

    public static class ARGB_BitmapExtractor implements BitmapExtractor {
        @Override
        public BufferedImage getImage(int width, int height, byte[] data) {
            int bytesPerPixel = 4;

            @SuppressWarnings("UndesirableClassUsage")
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < height; y++) {
                int stride = y * width;
                for (int x = 0; x < width; x++) {
                    int i = (stride + x) * bytesPerPixel;
                    int rgba = 0;
                    rgba |= ((int)data[i] & 0x000000ff);           // r
                    rgba |= ((int)data[i + 1] & 0x000000ff) << 8;  // g
                    rgba |= ((int)data[i + 2] & 0x000000ff) << 16; // b
                    rgba |= ((int)data[i + 3] & 0x000000ff) << 24; // a
                    bufferedImage.setRGB(x, y, rgba);
                }
            }

            return bufferedImage;
        }
    }

    public static class RGB_BitmapExtractor implements BitmapExtractor {

        @Override
        public BufferedImage getImage(int width, int height, byte[] data) {
            int bytesPerPixel = 3;

            @SuppressWarnings("UndesirableClassUsage")
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < height; y++) {
                int stride = y * width;
                for (int x = 0; x < width; x++) {
                    int i = (stride + x) * bytesPerPixel;
                    int rgba = 0;
                    rgba |= ((int)data[i] & 0x000000ff);           // r
                    rgba |= ((int)data[i + 1] & 0x000000ff) << 8;  // g
                    rgba |= ((int)data[i + 2] & 0x000000ff) << 16; // b
                    rgba |= 0xff000000;                            // a
                    bufferedImage.setRGB(x, y, rgba);
                }
            }

            return bufferedImage;
        }
    }

    public static class GRAYSCALE_BitmapExtractor implements BitmapExtractor {
        @Override
        public BufferedImage getImage(int width, int height, byte[] data) {
            int bytesPerPixel = 1;

            @SuppressWarnings("UndesirableClassUsage")
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = (y * width + x) * bytesPerPixel;
                    int rgba = 0;
                    int grayVal = (int)data[i] & 0x000000ff;
                    rgba |= grayVal << 16;     // r
                    rgba |= grayVal << 8;      // g
                    rgba |= grayVal;           // b
                    rgba |= 0xff000000;        // a
                    bufferedImage.setRGB(x, y, rgba);
                }
            }

            return bufferedImage;
        }
    }
}
