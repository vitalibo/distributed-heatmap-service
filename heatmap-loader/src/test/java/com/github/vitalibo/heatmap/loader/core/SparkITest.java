package com.github.vitalibo.heatmap.loader.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.vitalibo.heatmap.loader.DataFrameSuiteBase;
import com.github.vitalibo.heatmap.loader.TestHelper;
import com.github.vitalibo.heatmap.loader.core.util.JsonSerDe;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class SparkITest extends DataFrameSuiteBase {

    @DataProvider
    public Object[][] samples() {
        return range(7);
    }

    @Test(dataProvider = "samples")
    public void testRange(int i) {
        /*
            Sample #0: seconds
            Sample #1: minutes
            Sample #2: hours
            Sample #3: days
            Sample #4: weeks
            Sample #5: months
            Sample #6: years
         */

        Map<String, String> params = JsonSerDe.fromJsonString(
            TestHelper.resourceAsString(
                TestHelper.resourcePath("sample%s/params.json", i)),
            new TypeReference<Map<String, String>>() {});
        Dataset<Row> expected = createDataFrame(
            TestHelper.resourcePath("sample%s/timerange.json", i),
            TestHelper.resourcePath("timerange.schema.json"));

        Dataset<Timestamp> actual = spark()
            .range(
                LocalDateTime.parse(params.get("lowerBound")),
                LocalDateTime.parse(params.get("upperBound")),
                ChronoUnit.valueOf(params.get("unit")));

        assertDataFrameEquals(actual.select("*"), expected, "value");
    }

}
