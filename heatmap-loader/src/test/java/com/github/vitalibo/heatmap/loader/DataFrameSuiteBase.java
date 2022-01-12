package com.github.vitalibo.heatmap.loader;

import com.github.vitalibo.heatmap.loader.core.Sink;
import com.github.vitalibo.heatmap.loader.core.Source;
import com.github.vitalibo.heatmap.loader.core.Spark;
import com.github.vitalibo.heatmap.loader.core.util.JsonSerDe;
import com.holdenkarau.spark.testing.JavaDataFrameSuiteBase;
import com.holdenkarau.spark.testing.JavaDatasetSuiteBase;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RuntimeConfig;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.IntStream;

public abstract class DataFrameSuiteBase {

    private final JavaDataFrameSuiteBase suiteBase;

    public DataFrameSuiteBase() {
        suiteBase = new JavaDataFrameSuiteBase();
    }

    @BeforeClass
    public void beforeClass() {
        suiteBase.runBefore();
        final SparkSession session = suiteBase.spark();
        final RuntimeConfig config = session.conf();
        config.set("spark.sql.session.timeZone", "UTC");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public void afterClass() {
        JavaDatasetSuiteBase.runAfterClass();
    }

    public Spark spark() {
        return new Spark(suiteBase.spark());
    }

    public Dataset<Row> createDataFrame(String resource, String schema) {
        return createDataFrame(resource, TestHelper.resourceAsStructType(schema));
    }

    public Dataset<Row> createDataFrame(String resource, StructType schema) {
        final JavaRDD<String> rdd = suiteBase.spark()
            .sparkContext()
            .wholeTextFiles(Objects.requireNonNull(
                    getClass().getResource(resource),
                    String.format("%s not found", resource))
                .getFile(), 1)
            .toJavaRDD()
            .map(Tuple2::_2);

        return suiteBase.spark()
            .read()
            .option("multiLine", true)
            .option("mode", "PERMISSIVE")
            .schema(schema)
            .json(rdd);
    }

    public void assertDataFrameEquals(Dataset<Row> actual, Dataset<Row> expected, String... orderBy) {
        assertDataFrameEquals(actual, expected, false, orderBy);
    }

    public void assertDataFrameEquals(Dataset<Row> actual, Dataset<Row> expected,
                                      boolean ignoreSchema, String... orderBy) {
        if (actual == expected) {
            return;
        }

        if (orderBy.length > 0) {
            actual = actual.orderBy(orderBy[0], Arrays.stream(orderBy).skip(1).toArray(String[]::new));
        }

        if (!ignoreSchema) {
            Assert.assertEquals(actual.schema(), expected.schema());
        }

        String explanation = null;
        try {
            if (actual == null || expected == null) {
                explanation = "DataFrames not equal: expected: " + expected + " and actual: " + actual;
                Assert.fail();
            }

            actual = actual.cache();
            expected = expected.cache();

            final Collection<Object> act = toJsonCollection(actual);
            final Collection<Object> exp = toJsonCollection(expected);

            explanation = "DataFrames don't have the same size";
            Assert.assertEquals(act.size(), exp.size());

            Iterator<?> actIt = act.iterator();
            Iterator<?> expIt = exp.iterator();
            int i = -1;
            while (actIt.hasNext() && expIt.hasNext()) {
                i++;
                explanation = "DataFrames differ at element [" + i + "]";
                Assert.assertEquals(actIt.next(), expIt.next());
            }
        } catch (AssertionError ignored) {
            String actStr = actual != null ? actual.showString(60, 0, false) : null;
            String expStr = expected != null ? expected.showString(60, 0, false) : null;
            Assert.assertEquals(actStr, expStr, explanation);
        }
    }

    public Source<Row> createSource(String resource, String schema) {
        return spark -> createDataFrame(resource, schema);
    }

    public Source<Row> createSource(String resource, StructType schema) {
        return spark -> createDataFrame(resource, schema);
    }

    public Sink<Row> createSink(String resource, String schema, String... orderBy) {
        return (spark, actual) -> assertDataFrameEquals(actual, createDataFrame(resource, schema), false, orderBy);
    }

    public Sink<Row> createSink(String resource, StructType schema, String... orderBy) {
        return (spark, actual) -> assertDataFrameEquals(actual, createDataFrame(resource, schema), false, orderBy);
    }

    public Sink<Row> createSink(String resource, String schema, boolean ignoreSchema, String... orderBy) {
        return (spark, actual) -> assertDataFrameEquals(actual, createDataFrame(resource, schema), ignoreSchema, orderBy);
    }

    public Sink<Row> createSink(String resource, StructType schema, boolean ignoreSchema, String... orderBy) {
        return (spark, actual) -> assertDataFrameEquals(actual, createDataFrame(resource, schema), ignoreSchema, orderBy);
    }

    public Object[][] range(int endExclusive) {
        return range(0, endExclusive);
    }

    public Object[][] range(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
            .mapToObj(i -> new Object[]{i})
            .toArray(Object[][]::new);
    }

    private static Collection<Object> toJsonCollection(Dataset<Row> df) {
        return df
            .toJSON()
            .toJavaRDD()
            .map(o -> JsonSerDe.fromJsonString(o, Object.class))
            .collect();
    }

}
