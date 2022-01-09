package com.github.vitalibo.heatmap.loader.core;

import org.apache.spark.sql.Dataset;

@FunctionalInterface
public interface Sink<T> {

    void write(Spark spark, Dataset<T> dataset);

}
