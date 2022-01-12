package com.github.vitalibo.heatmap.loader.core.job;

import com.github.vitalibo.heatmap.loader.core.Job;
import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Source;
import com.github.vitalibo.heatmap.loader.core.Spark;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.explode;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.lower;
import static org.apache.spark.sql.functions.split;

@RequiredArgsConstructor
public class WordCountJob implements Job {

    private final Source<Row> source;
    private final Sink<Row> sink;

    @Override
    public void process(Spark spark) {
        Dataset<Row> df = source.read(spark)
            .withColumn("words", split(lower(col("text")), "\\W+"))
            .withColumn("word", explode(col("words")))
            .where(col("word").notEqual(lit("")))
            .groupBy("word")
            .count();

        spark.load(sink, df);
    }

}
