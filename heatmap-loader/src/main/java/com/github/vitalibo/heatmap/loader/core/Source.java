package com.github.vitalibo.heatmap.loader.core;

import org.apache.spark.sql.Dataset;

@FunctionalInterface
public interface Source<T> {

    Dataset<T> read(Spark spark);

}
