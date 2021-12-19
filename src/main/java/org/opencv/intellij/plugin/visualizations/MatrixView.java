package org.opencv.intellij.plugin.visualizations;

import org.opencv.intellij.plugin.MatDetails;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

public class MatrixView {

    public static final int FONT_SIZE = 20;

    public static boolean checkFont(String font) {
        String[] localFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (String localFont: localFonts) {
            if (localFont.equals(font)) {
                return true;
            }
        }
        return false;
    }

    private static Font getFont() {
        String[] preferredFonts = { "CMU Serif", "Bodoni 72" };
        Font font = null;
        for (String preferredFont: preferredFonts) {
            if (checkFont(preferredFont)) {
                return new Font(preferredFont, Font.PLAIN, FONT_SIZE);
            }
        }
        return new Font(Font.SERIF, Font.PLAIN, FONT_SIZE);
    }

    private static String makeLabel(boolean isIntType, double val) {
        if (isIntType) {
            return String.format("%d", (int)val);
        } else {
            int exponent = 1 + (int)log10(abs(val));
            if (val == (double)(int)val && val < 10000 && val > -10000) {
                return String.format("%d", (int)val);
            } else if (exponent <= 5 && exponent >= -1) {
                return String.format(String.format("%%%d.%df", 6, min(5 - exponent, 4)), val);
            } else {
                return String.format("%.2e", val).replace("e+0", "e");
            }
        }
    }

    public static BufferedImage drawView(double[] elements, MatDetails matDetails) {
        Font font = MatrixView.getFont();

        @SuppressWarnings("UndesirableClassUsage")
        Graphics tempGraphics = new BufferedImage(matDetails.getWidth(), matDetails.getHeight(), BufferedImage.TYPE_INT_ARGB).getGraphics();
        FontMetrics metrics = tempGraphics.getFontMetrics(font);
        int height = metrics.getHeight();
        Map<String, Integer> widthMap = new HashMap<>();

        boolean isIntType = matDetails.getDepth() <= MatDetails.CVTYPE_32S;
        List<String> labels = new ArrayList<>();
        int maxWidth = 0;
        for (double element: elements) {
            String label = makeLabel(isIntType, element);
            labels.add(label);
            int width = metrics.stringWidth(label);
            if (width > maxWidth) {
                maxWidth = width;
            }
            widthMap.put(label, width);
        }
        tempGraphics.dispose();

        int rowGap = 6;
        int colGap = 6;
        int borderGap = 8;
        int lineThickness = 3;
        int lipWidth = 6;
        int imageWidth = 2 * (borderGap + lipWidth) + maxWidth * matDetails.getWidth() + colGap * (matDetails.getWidth() - 1);
        int imageHeight = 2 * (borderGap + lipWidth) + height * matDetails.getHeight() + rowGap * (matDetails.getHeight() - 1);

        @SuppressWarnings("UndesirableClassUsage")
        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, imageWidth, imageHeight);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(lineThickness));
        g.draw(new Line2D.Float(borderGap, borderGap, borderGap + lipWidth, borderGap));
        g.draw(new Line2D.Float(borderGap, borderGap, borderGap, imageHeight - borderGap));
        g.draw(new Line2D.Float(borderGap, imageHeight - borderGap, borderGap + lipWidth, imageHeight - borderGap));
        g.draw(new Line2D.Float(imageWidth - borderGap, borderGap, imageWidth - borderGap - lipWidth, borderGap));
        g.draw(new Line2D.Float(imageWidth - borderGap, borderGap, imageWidth - borderGap, imageHeight - borderGap));
        g.draw(new Line2D.Float(imageWidth - borderGap, imageHeight - borderGap, imageWidth - borderGap - lipWidth, imageHeight - borderGap));
        g.setFont(font);
        int index = 0;
        for (String label: labels) {
            int width = widthMap.get(label);
            int row = index / matDetails.getWidth();
            int col = index % matDetails.getWidth();
            int x = borderGap + lipWidth + col * (maxWidth + colGap) + (maxWidth - width) / 2;
            int y = borderGap + lipWidth + row * (height + rowGap);
            g.drawString(label, x, y + FONT_SIZE);
            index++;
        }
        g.dispose();
        return bufferedImage;
    }
}
