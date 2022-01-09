package com.github.vitalibo.heatmap.loader.infrastructure.hbase;

import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Spark;
import com.github.vitalibo.heatmap.loader.infrastructure.hbase.transform.RowTranslator;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@RequiredArgsConstructor
public class HBaseHeatmapSink implements Sink<Row> {

    private final Configuration configuration;
    private final RowTranslator translator;

    public HBaseHeatmapSink(Configuration configuration, String tableName) {
        this(configuration, new RowTranslator(tableName));
    }

    @Override
    public void write(Spark spark, Dataset<Row> dataframe) {
        dataframe
            .toJavaRDD()
            .flatMapToPair(translator::from)
            .saveAsNewAPIHadoopFile(
                "",
                ImmutableBytesWritable.class,
                Put.class,
                TableOutputFormat.class,
                configuration);
    }

}
