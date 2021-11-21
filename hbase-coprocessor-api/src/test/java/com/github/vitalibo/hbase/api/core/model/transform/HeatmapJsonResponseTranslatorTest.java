package com.github.vitalibo.hbase.api.core.model.transform;

import com.github.vitalibo.hbase.api.core.model.Heatmap;
import com.github.vitalibo.hbase.api.core.model.HeatmapJsonResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HeatmapJsonResponseTranslatorTest {

    private Heatmap heatmap;

    @BeforeMethod
    public void setUp() {
        heatmap = new Heatmap(5, 3, null);
        heatmap.setScore(new double[][]{
            {0.0, 1.1, 0.0, 0.0, 0.0},
            {0.0, 0.0, 2.2, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 3.3}
        });
    }

    @Test
    public void testFromSparseHeatmap() {
        HeatmapJsonResponse actual = HeatmapJsonResponseTranslator.from(heatmap, 0.201);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getWidth(), 5);
        Assert.assertEquals(actual.getHeight(), 3);
        Assert.assertNull(actual.getDense());
        Assert.assertEquals(actual.getSparse(), new double[][]{
            {1.0, 0.0, 1.1},
            {2.0, 1.0, 2.2},
            {4.0, 2.0, 3.3}
        });
    }

    @Test
    public void testFromDenseHeatmap() {
        HeatmapJsonResponse actual = HeatmapJsonResponseTranslator.from(heatmap, 0.199);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getWidth(), 5);
        Assert.assertEquals(actual.getHeight(), 3);
        Assert.assertNull(actual.getSparse());
        Assert.assertEquals(actual.getDense(), new double[][]{
            {0.0, 1.1, 0.0, 0.0, 0.0},
            {0.0, 0.0, 2.2, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 3.3}
        });
    }

}
