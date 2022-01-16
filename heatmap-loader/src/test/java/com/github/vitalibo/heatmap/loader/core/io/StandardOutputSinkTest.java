package com.github.vitalibo.heatmap.loader.core.io;

import com.github.vitalibo.heatmap.loader.DataFrameSuiteBase;
import com.github.vitalibo.heatmap.loader.TestHelper;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StandardOutputSinkTest extends DataFrameSuiteBase {

    @Test
    public void testWrite() {
        Dataset<Row> dataframe = this
            .createDataFrame(
                TestHelper.resourcePath("dataframe.json"),
                TestHelper.resourcePath("dataframe.schema.json"))
            .coalesce(1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StandardOutputSink<Row> sink = new StandardOutputSink<>(new PrintStream(baos));
        sink.write(spark(), dataframe);
        String actual = baos.toString();

        Assert.assertNotNull(actual);
        Assert.assertFalse(actual.isEmpty());
        Assert.assertEquals(actual, TestHelper.resourceAsString(TestHelper.resourcePath("expected.txt")));
    }

}
