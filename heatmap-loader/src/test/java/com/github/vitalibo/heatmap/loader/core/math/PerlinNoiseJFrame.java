package com.github.vitalibo.heatmap.loader.core.math;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class PerlinNoiseJFrame extends JFrame {

    public PerlinNoiseJFrame() {
        super("Perlin Noise");
        NoiseJPanel panel = new NoiseJPanel(3.0);
        final Timer t = new Timer(30, o -> panel.repaint());
        t.start();

        this.add(panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(640, 480);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PerlinNoiseJFrame::new);
    }

    @AllArgsConstructor
    public static class NoiseJPanel extends JPanel {

        @Getter(lazy = true)
        private final BufferedImage bufferedImage
            = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        private final double stretch;
        private LocalDateTime zCoord;

        public NoiseJPanel(double stretch) {
            this(stretch, LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIN));
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(repaintNoise(), 0, 0, this);
        }

        private BufferedImage repaintNoise() {
            final BufferedImage img = this.getBufferedImage();
            for (int y = 0; y < this.getHeight(); y++) {
                for (int x = 0; x < this.getWidth(); x++) {
                    double dx = (double) x / this.getWidth() * stretch;
                    double dy = (double) y / this.getHeight() * stretch;
                    double noise = PerlinNoise.noise(dx, dy, Timestamp.valueOf(zCoord));
                    int color = (int) (((noise + 1) / 2) * 0xFF) * 0x10101;
                    img.setRGB(x, y, color);
                }
            }

            zCoord = zCoord.plusHours(1);
            return img;
        }

    }

}
