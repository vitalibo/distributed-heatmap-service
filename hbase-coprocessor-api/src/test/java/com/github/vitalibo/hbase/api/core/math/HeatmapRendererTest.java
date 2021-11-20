package com.github.vitalibo.hbase.api.core.math;

import com.github.vitalibo.hbase.api.core.model.Heatmap;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.image.BufferedImage;
import java.util.Random;

public class HeatmapRendererTest {

    private HeatmapRenderer renderer;

    @BeforeMethod
    public void setUp() {
        renderer = new HeatmapRenderer();
    }

    @Test
    public void testRender() {
        Heatmap heatmap = randomHeatmap(1, 100);

        BufferedImage actual = renderer.render(heatmap, 64, 1.0);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getWidth(), 320);
        Assert.assertEquals(actual.getHeight(), 240);
    }

    public static Heatmap randomHeatmap(int id, int count) {
        final Random random = new Random(id);
        final double[][] score = new double[240][320];
        for (int i = 0; i < count; i++) {
            score[random.nextInt(240)][random.nextInt(320)] = random.nextDouble();
        }

        return new Heatmap(320, 240, score);
    }

}
