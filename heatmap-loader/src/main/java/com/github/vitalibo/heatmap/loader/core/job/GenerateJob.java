package com.github.vitalibo.heatmap.loader.core.job;

import com.github.vitalibo.heatmap.loader.core.Heatmap;
import com.github.vitalibo.heatmap.loader.core.Job;
import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Spark;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;

import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;

@RequiredArgsConstructor
public class GenerateJob implements Job {

    private final Dimension dimension;
    private final List<String> labels;
    private final LocalDateTime lower;
    private final LocalDateTime upper;

    private final Heatmap heatmap;
    private final Sink<Row> sink;

    @Override
    public void process(Spark spark) {
        Dataset<Row> timeseries = spark
            .range(lower, upper, ChronoUnit.HOURS)
            .withColumnRenamed("value", "timestamp");

        Dataset<Row> heatmaps = spark
            .createDataset(labels, Encoders.STRING())
            .select(
                col("value").as("id"),
                lit((int) dimension.getWidth()).as("width"),
                lit((int) dimension.getHeight()).as("height"))
            .crossJoin(timeseries)
            .withColumn("heatmap", heatmap.generate(
                col("id"),
                col("width"),
                col("height"),
                col("timestamp")));

        spark.load(sink, heatmaps);
    }

}
