package com.github.vitalibo.heatmap.loader.core.io;

import com.github.vitalibo.heatmap.loader.core.Source;
import com.github.vitalibo.heatmap.loader.core.Spark;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@RequiredArgsConstructor
public class FileJsonSource implements Source<Row> {

    private final String path;

    @Override
    public Dataset<Row> read(Spark spark) {
        return spark.read()
            .json(path);
    }

}
