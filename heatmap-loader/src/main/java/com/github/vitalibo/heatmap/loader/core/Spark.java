package com.github.vitalibo.heatmap.loader.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;

import java.io.Closeable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Spark implements Closeable {

    @Delegate
    private final SparkSession session;
    @Getter(lazy = true)
    private final SparkConf sparkConf = session
        .sparkContext()
        .getConf();

    public void submit(Job job) {
        job.process(this);
    }

    public <T> Dataset<T> extract(Source<T> source) {
        return source.read(this);
    }

    public <T> void load(Sink<T> sink, Dataset<T> dataset) {
        sink.write(this, dataset);
    }

    public Dataset<Timestamp> range(LocalDateTime lowerBound, LocalDateTime upperBound, ChronoUnit unit) {
        return createDataset(
            Stream.iterate(lowerBound, o -> o.plus(1, unit))
                .limit(unit.between(lowerBound, upperBound.minus(1, ChronoUnit.MILLIS)) + 1)
                .map(Timestamp::valueOf)
                .collect(Collectors.toList()),
            Encoders.TIMESTAMP());
    }

    public int executorInstances() {
        return getSparkConf()
            .getInt("spark.executor.instances", 1);
    }

    public int executorCores() {
        return getSparkConf()
            .getInt("spark.executor.cores", 1);
    }

    public int totalCores() {
        return executorInstances() * executorCores();
    }

}
