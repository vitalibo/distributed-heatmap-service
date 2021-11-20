package com.github.vitalibo.hbase.api.infrastructure.hbase;

import com.github.vitalibo.hbase.api.core.Repository;
import com.github.vitalibo.hbase.api.core.model.Heatmap;
import com.github.vitalibo.hbase.api.core.model.HeatmapRangeQuery;
import com.github.vitalibo.hbase.api.infrastructure.hbase.transform.HeatmapTranslator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class HBaseRepository implements Repository {

    private static final Logger logger = LoggerFactory.getLogger(HBaseRepository.class);

    private final Table table;
    private final BiFunction<Result, ResultScanner, Heatmap> translator;

    public HBaseRepository(Table table) {
        this(table, HeatmapTranslator::from);
    }

    @Override
    @SneakyThrows
    public Heatmap queryByRange(HeatmapRangeQuery query) {
        final long measurementStartTime = System.currentTimeMillis();

        Result result = table.get(new Get(metaKey(query.getId())));

        ResultScanner scanResults = table.getScanner(new Scan()
            .withStartRow(rangeKey(query.getId(), query.getUnit()))
            .withStopRow(rangeKey(query.getId(), query.getFrom())));

        Heatmap heatmap = translator.apply(result, scanResults);

        logger.info("Querying heatmap from Hbase took {} millis", System.currentTimeMillis() - measurementStartTime);
        return heatmap;
    }

    private static byte[] metaKey(long id) {
        return Bytes.toBytes(String.format("%s", id));
    }

    private static byte[] rangeKey(long id, LocalDateTime datetime) {
        return Bytes.toBytes(String.format("%s:%s",
            id, Long.MAX_VALUE - datetime.toEpochSecond(ZoneOffset.UTC)));
    }

}
