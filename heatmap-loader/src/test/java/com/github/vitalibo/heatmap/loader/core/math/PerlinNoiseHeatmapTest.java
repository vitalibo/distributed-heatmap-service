package com.github.vitalibo.heatmap.loader.core.math;

import com.github.vitalibo.heatmap.loader.core.Heatmap;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Timestamp;

public class PerlinNoiseHeatmapTest {

    @DataProvider
    public Object[][] samples() {
        return new Object[][]{
            {"foo", "00:00:00", 640, 480, 0.0, 1.0},
            {"foo", "00:00:00", 640, 480, 0.0, 10.0},
            {"foo", "01:00:00", 640, 480, 0.0, 1.0},
            {"bar", "00:00:00", 640, 480, 0.0, 1.0},
            {"foo", "00:00:00", 320, 280, 0.0, 1.0},
            {"foo", "00:00:00", 640, 480, 10.0, 20.0}
        };
    }

    @Test(dataProvider = "samples")
    public void testGenerate(String id, String time, int width, int height, double min, double max) {
        Heatmap heatmap = new PerlinNoiseHeatmap(5.0, min, max, 1.0);

        double[][] actual = heatmap.generate(id, width, height, Timestamp.valueOf("2022-01-02 " + time));

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.length, height);
        for (double[] row : actual) {
            Assert.assertEquals(row.length, width);
            for (double o : row) {
                Assert.assertTrue(o >= min);
                Assert.assertTrue(o <= max);
            }
        }
    }

}
