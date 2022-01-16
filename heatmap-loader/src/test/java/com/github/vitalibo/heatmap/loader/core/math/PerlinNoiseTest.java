package com.github.vitalibo.heatmap.loader.core.math;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.IntStream;

public class PerlinNoiseTest {

    @DataProvider
    public Object[][] samples() {
        return new Object[][]{
            {0.0, 0.0, 0.0, 0.000000},
            {0.1, 0.0, 0.0, +0.091440}, {0.0, 0.1, 0.0, -0.007704}, {0.0, 0.0, 0.1, 0.106848},
            {0.2, 0.0, 0.0, +0.142079}, {0.0, 0.2, 0.0, -0.046336}, {0.0, 0.0, 0.2, 0.234752},
            {0.3, 0.0, 0.0, +0.136919}, {0.0, 0.3, 0.0, -0.114156}, {0.0, 0.0, 0.3, 0.365232},
            {0.4, 0.0, 0.0, +0.082559}, {0.0, 0.4, 0.0, -0.190464}, {0.0, 0.0, 0.4, 0.463488},
            {0.5, 0.0, 0.0, +0.000000}, {0.0, 0.5, 0.0, -0.250000}, {0.0, 0.0, 0.5, 0.500000},
            {0.6, 0.0, 0.0, -0.082560}, {0.0, 0.6, 0.0, -0.273024}, {0.0, 0.0, 0.6, 0.463488},
            {0.7, 0.0, 0.0, -0.136919}, {0.0, 0.7, 0.0, -0.251076}, {0.0, 0.0, 0.7, 0.365232},
            {0.8, 0.0, 0.0, -0.142080}, {0.0, 0.8, 0.0, -0.188415}, {0.0, 0.0, 0.8, 0.234751},
            {0.9, 0.0, 0.0, -0.091439}, {0.0, 0.9, 0.0, -0.099143}, {0.0, 0.0, 0.9, 0.106848},
            {1.0, 0.0, 0.0, +0.000000}, {0.0, 1.0, 0.0, +0.000000}, {0.0, 0.0, 1.0, 0.000000},
            {1.0, 1.0, 1.0, 0.000000}
        };
    }

    @Test(dataProvider = "samples")
    public void testNoise(double x, double y, double z, double expected) {
        double actual = PerlinNoise.noise(x, y, z);

        Assert.assertEquals(actual, expected, 1E-6);
    }

    @DataProvider
    public Object[][] random() {
        return IntStream.range(0, 1000)
            .mapToObj(i -> new Object[]{Math.random(), Math.random(), Math.random()})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "random")
    public void testNoiseRange(double x, double y, double z) {
        double actual = PerlinNoise.noise(x, y, z);

        Assert.assertTrue(actual >= -1.0);
        Assert.assertTrue(actual <= 1.0);
    }

}
