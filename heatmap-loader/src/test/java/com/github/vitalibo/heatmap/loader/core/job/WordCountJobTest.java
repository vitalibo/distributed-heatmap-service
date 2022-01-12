package com.github.vitalibo.heatmap.loader.core.job;


import com.github.vitalibo.heatmap.loader.DataFrameSuiteBase;
import com.github.vitalibo.heatmap.loader.TestHelper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class WordCountJobTest extends DataFrameSuiteBase {

    @DataProvider
    public Object[][] samples() {
        return range(3);
    }

    @Test(dataProvider = "samples")
    public void testJob(int i) {
        /*
           Sample #1: Default dataset
           Sample #2: Ignore case
           Sample #3: Different separators
        */

        WordCountJob job = new WordCountJob(
            createSource(
                TestHelper.resourcePath("sample%s/source.json", i),
                TestHelper.resourcePath("source.schema.json")),
            createSink(
                TestHelper.resourcePath("sample%s/sink.json", i),
                TestHelper.resourcePath("sink.schema.json"),
                "word"));

        job.process(spark());
    }

}
