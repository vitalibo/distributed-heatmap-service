package com.github.vitalibo.heatmap.loader.core.io;

import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Spark;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;

import java.io.PrintStream;

@RequiredArgsConstructor
public class StandardOutputSink<T> implements Sink<T> {

    private final PrintStream stream;

    public StandardOutputSink() {
        this(System.out);
    }

    @Override
    public void write(Spark spark, Dataset<T> dataset) {
        stream.println(dataset.schema().treeString(Integer.MAX_VALUE));
        stream.println();
        stream.println(dataset.showString(Integer.MAX_VALUE, -1, false));
        stream.flush();
    }

}
