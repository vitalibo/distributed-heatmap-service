package com.github.vitalibo.hbase.plugin;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellBuilderFactory;
import org.apache.hadoop.hbase.CellBuilderType;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessor;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.coprocessor.RegionObserver;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HeatmapAggregationRegionCoprocessor implements RegionCoprocessor, RegionObserver {

    private static final Logger logger = LoggerFactory.getLogger(HeatmapAggregationRegionCoprocessor.class);

    @Override
    public Optional<RegionObserver> getRegionObserver() {
        return Optional.of(this);
    }

    @Override
    public boolean postScannerNext(ObserverContext<RegionCoprocessorEnvironment> context, InternalScanner scanner,
                                   List<Result> results, int limit, boolean hasNext) throws IOException {
        final long measurementStartTime = System.currentTimeMillis();
        int measureAggregatedHeatmaps = 0;

        final List<Cell> cells = new ArrayList<>();
        results.forEach(result -> cells.addAll(result.listCells()));
        if (cells.isEmpty()) {
            return RegionObserver.super.postScannerNext(context, scanner, results, limit, hasNext);
        }

        final Cell defaultCell = cells.get(0);
        double[] score = new double[Snappy.uncompressDoubleArray(CellUtil.cloneValue(defaultCell)).length];
        double[] newScore;
        do {
            for (Cell cell : cells) {
                newScore = Snappy.uncompressDoubleArray(CellUtil.cloneValue(cell));
                for (int i = 0; i < score.length; i++) {
                    score[i] += newScore[i];
                }
                measureAggregatedHeatmaps++;
            }

            cells.clear();
        } while (scanner.next(cells) || !cells.isEmpty());

        Cell cell = CellBuilderFactory.create(CellBuilderType.DEEP_COPY)
            .setRow(CellUtil.cloneRow(defaultCell))
            .setFamily(CellUtil.cloneFamily(defaultCell))
            .setQualifier(CellUtil.cloneQualifier(defaultCell))
            .setTimestamp(defaultCell.getTimestamp())
            .setType(defaultCell.getType())
            .setValue(Snappy.compress(score))
            .build();

        results.clear();
        results.add(Result.create(Collections.singletonList(cell)));

        logger.info("Aggregate {} heatmaps in {} millis",
            measureAggregatedHeatmaps, System.currentTimeMillis() - measurementStartTime);
        return RegionObserver.super.postScannerNext(context, scanner, results, limit, hasNext);
    }

}
