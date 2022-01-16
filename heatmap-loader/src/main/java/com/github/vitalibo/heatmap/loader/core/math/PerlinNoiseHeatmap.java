package com.github.vitalibo.heatmap.loader.core.math;

import com.github.vitalibo.heatmap.loader.core.Heatmap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Timestamp;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class PerlinNoiseHeatmap implements Heatmap {

    private final double stretch;
    private final double min;
    private final double max;
    private final double pow;

    public PerlinNoiseHeatmap() {
        this(3.0, 0.0, 1.0, 8);
    }

    @Override
    @SneakyThrows
    public double[][] generate(String id, Integer width, Integer height, Timestamp timestamp) {
        int hash = id.hashCode();
        int shiftX = hash & 0xFFFF;
        int shiftY = hash >> 16;

        return IntStream.range(0, height)
            .mapToDouble(y -> (double) y / height)
            .mapToObj(dy -> IntStream.range(0, width)
                .mapToDouble(x -> (double) x / width)
                .map(dx -> PerlinNoise.noise(dx * stretch + shiftX, dy * stretch + shiftY, timestamp))
                .map(o -> ((1 + o) / 2))
                .map(o -> Math.pow(o, pow) * (max - min) + min)
                .toArray())
            .toArray(double[][]::new);
    }

}
