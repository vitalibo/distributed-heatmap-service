package com.github.vitalibo.heatmap.api.infrastructure.mock;

import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRangeQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

public class RandomHeatmapRepositoryTest {

    @Test
    public void testQueryByRange() {
        RandomHeatmapRepository repository = new RandomHeatmapRepository(20, 10);
        HeatmapRangeQuery query = new HeatmapRangeQuery();
        query.setId(1L);
        query.setFrom(LocalDateTime.of(2021, 10, 30, 0, 0));
        query.setUnit(LocalDateTime.of(2021, 10, 30, 0, 8));

        Heatmap actual = repository.queryByRange(query);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getWidth(), 20);
        Assert.assertEquals(actual.getHeight(), 10);
        long count = Arrays.stream(actual.getScore())
            .flatMapToDouble(Arrays::stream)
            .filter(o -> o > 0)
            .count();
        Assert.assertEquals(count, 8);
        double[][] score = actual.getScore();
        Assert.assertEquals(score[5][8], 0.41008081149220166);
        Assert.assertEquals(score[2][2], 0.9370821488959696);
        Assert.assertEquals(score[6][12], 0.34751802920311026);
    }

}
