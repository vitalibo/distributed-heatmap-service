package com.github.vitalibo.heatmap.loader.core.io;

import com.github.vitalibo.heatmap.loader.DataFrameSuiteBase;
import com.github.vitalibo.heatmap.loader.TestHelper;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class FileJsonSinkTest extends DataFrameSuiteBase {

    @Test
    public void testWrite() throws IOException {
        Path tempDir = Files.createTempDirectory("jsonSink-");
        Dataset<Row> dataframe = this
            .createDataFrame(
                TestHelper.resourcePath("dataframe.json"),
                TestHelper.resourcePath("dataframe.schema.json"))
            .coalesce(1);

        FileJsonSink sink = new FileJsonSink(String.valueOf(tempDir));
        sink.write(spark(), dataframe);

        Path[] files = Files.list(tempDir).filter(o -> String.valueOf(o).endsWith(".json")).toArray(Path[]::new);
        Assert.assertEquals(files.length, 1);
        String actual = Files.lines(files[0]).collect(Collectors.joining(System.lineSeparator()));
        Assert.assertEquals(actual, TestHelper.resourceAsString(TestHelper.resourcePath("expected.txt")));
    }

}
