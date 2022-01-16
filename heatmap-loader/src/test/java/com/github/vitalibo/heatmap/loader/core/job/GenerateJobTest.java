package com.github.vitalibo.heatmap.loader.core.job;

import com.github.vitalibo.heatmap.loader.DataFrameSuiteBase;
import com.github.vitalibo.heatmap.loader.TestHelper;
import com.github.vitalibo.heatmap.loader.core.math.RandomHeatmap;
import org.testng.annotations.Test;

import java.awt.Dimension;
import java.time.LocalDateTime;
import java.util.Arrays;

public class GenerateJobTest extends DataFrameSuiteBase {

    @Test
    public void testJob() {
        GenerateJob job = new GenerateJob(
            new Dimension(3, 2),
            Arrays.asList("#1", "#2"),
            LocalDateTime.parse("2022-02-01T00:00:00"),
            LocalDateTime.parse("2022-02-01T03:00:00"),
            new RandomHeatmap(1.0, 0.0, 10.0),
            createSink(
                TestHelper.resourcePath("sink.json"),
                TestHelper.resourcePath("sink.schema.json"),
                "id", "timestamp"));

        job.process(spark());
    }

}
