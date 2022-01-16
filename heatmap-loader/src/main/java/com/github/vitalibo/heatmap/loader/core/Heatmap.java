package com.github.vitalibo.heatmap.loader.core;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.api.java.UDF4;
import org.apache.spark.sql.types.DataTypes;

import java.io.Serializable;
import java.sql.Timestamp;

import static org.apache.spark.sql.functions.udf;

public interface Heatmap extends Serializable {

    default Column generate(Column id, Column width, Column height, Column timestamp) {
        UDF4<String, Integer, Integer, Timestamp, double[][]> func = this::generate;
        return udf(func, DataTypes.createArrayType(DataTypes.createArrayType(DataTypes.DoubleType)))
            .apply(id, width, height, timestamp);
    }

    double[][] generate(String id, Integer width, Integer height, Timestamp timestamp);

}
