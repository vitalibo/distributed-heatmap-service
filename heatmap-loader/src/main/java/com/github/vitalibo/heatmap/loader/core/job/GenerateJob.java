package com.github.vitalibo.heatmap.loader.core.job;

import com.github.vitalibo.heatmap.loader.core.Job;
import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Spark;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Row;

@RequiredArgsConstructor
public class GenerateJob implements Job {

    private final Sink<Row> sink;

    @Override
    public void process(Spark spark) {

    }

}
