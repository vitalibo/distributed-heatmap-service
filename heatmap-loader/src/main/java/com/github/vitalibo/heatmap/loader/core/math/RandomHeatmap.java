package com.github.vitalibo.heatmap.loader.core.math;

import com.github.vitalibo.heatmap.loader.core.Heatmap;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class RandomHeatmap implements Heatmap {

    private final double dense;
    private final double min;
    private final double max;

    public RandomHeatmap() {
        this(0.5, 0.0, 1.0);
    }

    @Override
    public double[][] generate(String id, Integer width, Integer height, Timestamp timestamp) {
        final Random random = new Random(Objects.hash(id, timestamp));

        return IntStream.range(0, height)
            .mapToObj(y -> IntStream.range(0, width)
                .mapToDouble(x -> random.nextDouble() < dense ? random.nextDouble() * (max - min) + min : 0.0)
                .toArray())
            .toArray(double[][]::new);
    }

}
