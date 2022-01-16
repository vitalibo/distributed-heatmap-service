package com.github.vitalibo.heatmap.loader.core.math;

import com.github.vitalibo.heatmap.loader.core.Heatmap;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Timestamp;

public class RandomHeatmapTest {

    @DataProvider
    public Object[][] samples() {
        return new Object[][]{
            {"foo", "00:00:00", 640, 480, 1.0, 0.0, 1.0},
            {"foo", "00:00:00", 640, 480, 1.0, 0.0, 10.0},
            {"foo", "01:00:00", 640, 480, 1.0, 0.0, 1.0},
            {"bar", "00:00:00", 640, 480, 1.0, 0.0, 1.0},
            {"foo", "00:00:00", 320, 280, 1.0, 0.0, 1.0},
            {"foo", "00:00:00", 640, 480, 1.0, 10.0, 20.0},
            {"foo", "00:00:00", 640, 480, 0.7, 0.0, 1.0},
        };
    }

    @Test(dataProvider = "samples")
    public void testGenerate(String id, String time, int width, int height,
                             double dense, double min, double max) {
        Heatmap heatmap = new RandomHeatmap(dense, min, max);

        double[][] actual = heatmap.generate(id, width, height, Timestamp.valueOf("2022-01-02 " + time));

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.length, height);
        int empty = 0;
        for (double[] row : actual) {
            Assert.assertEquals(row.length, width);
            for (double o : row) {
                if (o == 0.0) {
                    empty++;
                } else {
                    Assert.assertTrue(o >= min);
                }
                Assert.assertTrue(o <= max);
            }
        }
        Assert.assertEquals(1.0 * empty / (width * height), 1.0 - dense, 0.05);
    }

}
