package com.github.vitalibo.hbase.api.infrastructure.hbase.transform;

import com.github.vitalibo.hbase.api.core.model.Heatmap;
import lombok.SneakyThrows;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.xerial.snappy.Snappy;

import java.util.Optional;
import java.util.stream.StreamSupport;

public final class HeatmapTranslator {

    private static final byte[] META_FAMILY = Bytes.toBytes("META");
    private static final byte[] DATA_FAMILY = Bytes.toBytes("DATA");
    private static final byte[] WIDTH_QUALIFIER = Bytes.toBytes("W");
    private static final byte[] HEIGHT_QUALIFIER = Bytes.toBytes("H");
    private static final byte[] HEATMAP_QUALIFIER = Bytes.toBytes("HM");

    private HeatmapTranslator() {
    }

    public static Heatmap from(Result result, ResultScanner scanResults) {
        Optional<double[]> flatten = StreamSupport.stream(scanResults.spliterator(), true)
            .map(HeatmapTranslator::uncompressDoubleArray)
            .reduce(HeatmapTranslator::aggregate);

        if (!flatten.isPresent()) {
            return new Heatmap(1, 1, new double[][]{{0.0}});
        }

        int width = Bytes.toInt(result.getValue(META_FAMILY, WIDTH_QUALIFIER));
        int height = Bytes.toInt(result.getValue(META_FAMILY, HEIGHT_QUALIFIER));

        double[][] score = new double[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(flatten.get(), width * i, score[i], 0, width);
        }

        final Heatmap heatmap = new Heatmap();
        heatmap.setScore(score);
        heatmap.setWidth(width);
        heatmap.setHeight(height);
        return heatmap;
    }

    @SneakyThrows
    private static double[] uncompressDoubleArray(Result result) {
        return Snappy.uncompressDoubleArray(
            result.getValue(DATA_FAMILY, HEATMAP_QUALIFIER));
    }

    private static double[] aggregate(double[] left, double[] right) {
        if (left.length != right.length) {
            throw new IllegalArgumentException("Can't aggregate heatmap with different length.");
        }

        for (int i = 0; i < left.length; i++) {
            left[i] += right[i];
        }

        return left;
    }

}
