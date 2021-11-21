package com.github.vitalibo.heatmap.api.core.math;

import com.github.vitalibo.heatmap.api.core.Renderer;
import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public class HeatmapRenderer implements Renderer {

    private final BufferedImage heatpoint;
    private final int[] gradient;

    public HeatmapRenderer() {
        this.heatpoint = resourceAsBufferedImage("/renderer/heatpoint.png");
        BufferedImage gradient = resourceAsBufferedImage("/renderer/gradient.png");
        this.gradient = gradient.getRGB(0, 0, 1, gradient.getHeight(), null, 0, 1);
    }

    @Override
    public BufferedImage render(Heatmap heatmap, int radius, double opacity) {
        final HeatmapBufferedImage canvas = new HeatmapBufferedImage(heatmap);
        canvas.draw(radius, opacity);
        canvas.gradient();

        return canvas;
    }

    @SneakyThrows
    private static BufferedImage resourceAsBufferedImage(String resource) {
        return ImageIO.read(Objects.requireNonNull(Renderer.class.getResourceAsStream(resource)));
    }

    public class HeatmapBufferedImage extends BufferedImage {

        private final int width;
        private final int height;
        private final double[][] heatmap;

        public HeatmapBufferedImage(Heatmap heatmap) {
            super(heatmap.getWidth(), heatmap.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            this.heatmap = heatmap.getScore();
            this.height = heatmap.getHeight();
            this.width = heatmap.getWidth();

            final Graphics2D g = createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.dispose();
        }

        public void draw(int radius, double opacity) {
            final double maxScore = Arrays.stream(heatmap)
                .flatMapToDouble(Arrays::stream)
                .max()
                .orElse(1.0);

            final BufferedImage mask = resizeHeatpointMask(radius);
            int xShift = mask.getWidth() / 2;
            int yShift = mask.getHeight() / 2;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double score = heatmap[y][x];

                    if (score > 0) {
                        double opaque = Math.min((score / maxScore) * opacity, 1.0);

                        final Graphics2D g = createGraphics();
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opaque));
                        g.drawImage(mask, x - xShift, y - yShift, null);
                        g.dispose();
                    }
                }
            }
        }

        public void gradient() {
            final int[] buff = getRGB(0, 0, width, height, null, 0, width);
            int size = gradient.length - 1;

            for (int i = 0; i < buff.length; i++) {
                int rgb = buff[i];
                int r = 255 - ((rgb >>> 16) & 0xff);
                int g = 255 - ((rgb >>> 8) & 0xff);
                int b = 255 - (rgb & 0xff);

                double f = ((r * g * b) / 16581375.0) * size;
                buff[i] = gradient[(int) f];
            }

            setRGB(0, 0, width, height, buff, 0, width);
        }

        private BufferedImage resizeHeatpointMask(int size) {
            BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            g.drawImage(heatpoint.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();
            return canvas;
        }

    }

}
