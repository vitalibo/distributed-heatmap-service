package com.github.vitalibo.hbase.api.core.model.transform;

import com.github.vitalibo.hbase.api.core.model.Heatmap;
import com.github.vitalibo.hbase.api.core.model.HeatmapJsonResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HeatmapJsonResponseTranslator {

    private HeatmapJsonResponseTranslator() {
    }

    public static HeatmapJsonResponse from(Heatmap heatmap, double sparseHeatmapThreshold) {
        final boolean isSparseHeatmap = isSparseHeatmap(heatmap, sparseHeatmapThreshold);

        return new HeatmapJsonResponse()
            .withWidth(heatmap.getWidth())
            .withHeight(heatmap.getHeight())
            .withSparse(isSparseHeatmap ? asSparseHeatmapScoreArray(heatmap) : null)
            .withDense(isSparseHeatmap ? null : heatmap.getScore());
    }

    private static boolean isSparseHeatmap(Heatmap heatmap, double threshold) {
        double count = Arrays.stream(heatmap.getScore())
            .flatMapToDouble(Arrays::stream)
            .filter(o -> o > 0.0)
            .count();

        return count / (heatmap.getWidth() * heatmap.getHeight()) < threshold;
    }

    private static double[][] asSparseHeatmapScoreArray(Heatmap heatmap) {
        final List<double[]> cells = new ArrayList<>(heatmap.getWidth() * heatmap.getHeight() / 3);
        for (int x = 0; x < heatmap.getWidth(); x++) {
            for (int y = 0; y < heatmap.getHeight(); y++) {
                double v = heatmap.getScore()[y][x];
                if (v > 0.0) {
                    cells.add(new double[]{x, y, v});
                }
            }
        }

        return cells.toArray(new double[][]{});
    }

}
