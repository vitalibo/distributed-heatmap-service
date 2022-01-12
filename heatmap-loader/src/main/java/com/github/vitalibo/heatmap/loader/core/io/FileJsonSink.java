package com.github.vitalibo.heatmap.loader.core.io;

import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Spark;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

@RequiredArgsConstructor
public class FileJsonSink implements Sink<Row> {

    private final String path;
    private final SaveMode saveMode;

    public FileJsonSink(String path) {
        this(path, SaveMode.Append);
    }

    @Override
    public void write(Spark spark, Dataset<Row> dataframe) {
        dataframe
            .write()
            .mode(saveMode)
            .json(path);
    }

}
