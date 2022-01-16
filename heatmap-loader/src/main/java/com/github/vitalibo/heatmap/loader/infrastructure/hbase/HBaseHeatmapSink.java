package com.github.vitalibo.heatmap.loader.infrastructure.hbase;

import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Spark;
import com.github.vitalibo.heatmap.loader.infrastructure.hbase.transform.PutTranslator;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.flatten;

@RequiredArgsConstructor
public class HBaseHeatmapSink implements Sink<Row> {

    private final Configuration configuration;
    private final PairFlatMapFunction<Row, ImmutableBytesWritable, Put> translator;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public HBaseHeatmapSink(Configuration configuration,
                            String familyMeta, String familyData, String qualifierWidth,
                            String qualifierHeight, String qualifierHeatmap) {
        this(configuration,
            new PutTranslator(
                familyMeta, familyData,
                qualifierWidth, qualifierHeight, qualifierHeatmap)::from);
    }

    @Override
    public void write(Spark spark, Dataset<Row> dataframe) {
        dataframe
            .withColumn("heatmap", flatten(col("heatmap")))
            .toJavaRDD()
            .flatMapToPair(translator)
            .saveAsNewAPIHadoopFile(
                "",
                ImmutableBytesWritable.class,
                Put.class,
                TableOutputFormat.class,
                configuration);
    }

}
