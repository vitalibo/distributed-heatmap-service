package com.github.vitalibo.heatmap.loader.core.io;

import com.github.vitalibo.heatmap.loader.DataFrameSuiteBase;
import com.github.vitalibo.heatmap.loader.TestHelper;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.testng.annotations.Test;

public class FileJsonSourceTest extends DataFrameSuiteBase {

    @Test
    public void testRead() {
        Dataset<Row> expected = createDataFrame(
            TestHelper.resourcePath("expected.json"),
            TestHelper.resourcePath("expected.schema.json"));

        FileJsonSource source = new FileJsonSource(getClass()
            .getResource(TestHelper.resourcePath("input.txt"))
            .getFile());
        Dataset<Row> actual = source.read(spark());

        assertDataFrameEquals(actual, expected);
    }

}
