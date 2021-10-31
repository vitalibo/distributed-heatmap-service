package com.github.vitalibo.hbase.api.infrastructure.mock;

import com.github.vitalibo.hbase.api.core.Repository;
import com.github.vitalibo.hbase.api.core.model.Heatmap;
import com.github.vitalibo.hbase.api.core.model.HeatmapRangeQuery;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
public class RandomHeatmapRepository implements Repository {

    private final int width;
    private final int height;

    public RandomHeatmapRepository() {
        this(1280, 1024);
    }

    @Override
    public Heatmap queryByRange(HeatmapRangeQuery query) {
        final Random random = new Random(query.getId());
        final double[][] score = new double[width][height];

        LocalDateTime timestamp = query.getFrom();
        while (timestamp.isBefore(query.getUnit())) {
            timestamp = timestamp.plusMinutes(1);
            score[random.nextInt(width)][random.nextInt(height)] = random.nextDouble();
        }

        return new Heatmap()
            .withHeight(height)
            .withWidth(width)
            .withScore(score);
    }

}
