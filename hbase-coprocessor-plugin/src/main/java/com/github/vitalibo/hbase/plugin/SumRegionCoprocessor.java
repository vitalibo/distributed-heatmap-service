package com.github.vitalibo.hbase.plugin;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessor;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.coprocessor.RegionObserver;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.regionserver.RegionScanner;
import org.apache.hadoop.hbase.regionserver.ScanOptions;
import org.apache.hadoop.hbase.regionserver.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SumRegionCoprocessor implements RegionCoprocessor, RegionObserver {

    private static final Logger logger = LoggerFactory.getLogger(SumRegionCoprocessor.class);

    @Override
    public Optional<RegionObserver> getRegionObserver() {
        return Optional.of(this);
    }

    @Override
    public void preScannerOpen(ObserverContext<RegionCoprocessorEnvironment> c, Scan scan) throws IOException {
        logger.info("---------- preScannerOpen : ObserverContext={} Scan={}", c, scan);
        RegionObserver.super.preScannerOpen(c, scan);
    }

    @Override
    public RegionScanner postScannerOpen(ObserverContext<RegionCoprocessorEnvironment> c, Scan scan, RegionScanner s) throws IOException {
        logger.info("---------- postScannerOpen : ObserverContext={} Scan={} RegionScanner={}", c, scan, c);
        return RegionObserver.super.postScannerOpen(c, scan, s);
    }

    @Override
    public boolean preScannerNext(ObserverContext<RegionCoprocessorEnvironment> c, InternalScanner s, List<Result> result, int limit, boolean hasNext) throws IOException {
        logger.info("---------- preScannerNext : ObserverContext={} InternalScanner={} List<Result>={} Limit={} HasNext={}", c, s, result, limit, hasNext);
        return RegionObserver.super.preScannerNext(c, s, result, limit, hasNext);
    }

    @Override
    public boolean postScannerNext(ObserverContext<RegionCoprocessorEnvironment> c, InternalScanner s, List<Result> result, int limit, boolean hasNext) throws IOException {
        logger.info("---------- postScannerNext : ObserverContext={} InternalScanner={} List<Result>={} Limit={} HasNext={}", c, s, result, limit, hasNext);
        return RegionObserver.super.postScannerNext(c, s, result, limit, hasNext);
    }

    @Override
    public boolean postScannerFilterRow(ObserverContext<RegionCoprocessorEnvironment> c, InternalScanner s, Cell curRowCell, boolean hasMore) throws IOException {
        logger.info("---------- postScannerFilterRow : ObserverContext={} InternalScanner={} Cell={} HasMore={}", c, s, curRowCell, hasMore);
        return RegionObserver.super.postScannerFilterRow(c, s, curRowCell, hasMore);
    }

    @Override
    public void preScannerClose(ObserverContext<RegionCoprocessorEnvironment> c, InternalScanner s) throws IOException {
        logger.info("---------- preScannerClose : ObserverContext={} InternalScanner={}", c, s);
        RegionObserver.super.preScannerClose(c, s);
    }

    @Override
    public void postScannerClose(ObserverContext<RegionCoprocessorEnvironment> ctx, InternalScanner s) throws IOException {
        logger.info("---------- postScannerClose : ObserverContext={} InternalScanner={}", ctx, s);
        RegionObserver.super.postScannerClose(ctx, s);
    }

    @Override
    public void preStoreScannerOpen(ObserverContext<RegionCoprocessorEnvironment> ctx, Store store, ScanOptions options) throws IOException {
        logger.info("---------- preStoreScannerOpen : ObserverContext={} Store={} ScanOptions={}", ctx, store, options);
        RegionObserver.super.preStoreScannerOpen(ctx, store, options);
    }

}
