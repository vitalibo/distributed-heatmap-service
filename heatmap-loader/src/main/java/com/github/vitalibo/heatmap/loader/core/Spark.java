package com.github.vitalibo.heatmap.loader.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;

import java.io.Closeable;

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
