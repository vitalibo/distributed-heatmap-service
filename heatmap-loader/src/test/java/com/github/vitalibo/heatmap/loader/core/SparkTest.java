package com.github.vitalibo.heatmap.loader.core;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SparkTest {

    @Mock
    private SparkSession mockSparkSession;
    @Mock
    private SparkContext mockSparkContext;
    @Mock
    private SparkConf mockSparkConf;
    @Mock
    private Job mockJob;
    @Mock
    private Source<Row> mockSource;
    @Mock
    private Sink<Row> mockSink;
    @Mock
    private Dataset<Row> mockDataFrame;

    private Spark spySpark;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        Mockito.when(mockSparkSession.sparkContext()).thenReturn(mockSparkContext);
        Mockito.when(mockSparkContext.getConf()).thenReturn(mockSparkConf);
        spySpark = Mockito.spy(new Spark(mockSparkSession));
    }

    @Test
    public void testSubmit() {
        spySpark.submit(mockJob);

        Mockito.verify(mockJob).process(spySpark);
    }

    @Test
    public void testExtract() {
        Mockito.when(mockSource.read(Mockito.any())).thenReturn(mockDataFrame);

        Dataset<Row> actual = spySpark.extract(mockSource);

        Assert.assertSame(actual, mockDataFrame);
        Mockito.verify(mockSource).read(spySpark);
    }

    @Test
    public void testSink() {
        spySpark.load(mockSink, mockDataFrame);

        Mockito.verify(mockSink).write(spySpark, mockDataFrame);
    }

    @Test
    public void testExecutorInstances() {
        Mockito.when(mockSparkConf.getInt(Mockito.anyString(), Mockito.anyInt())).thenReturn(2);

        int actual = spySpark.executorInstances();

        Assert.assertEquals(actual, 2);
        Mockito.verify(mockSparkConf).getInt("spark.executor.instances", 1);
    }

    @Test
    public void testExecutorCores() {
        Mockito.when(mockSparkConf.getInt(Mockito.anyString(), Mockito.anyInt())).thenReturn(2);

        int actual = spySpark.executorCores();

        Assert.assertEquals(actual, 2);
        Mockito.verify(mockSparkConf).getInt("spark.executor.cores", 1);
    }

    @Test
    public void testTotalCores() {
        Mockito.doReturn(3).when(spySpark).executorInstances();
        Mockito.doReturn(2).when(spySpark).executorCores();

        int actual = spySpark.totalCores();

        Assert.assertEquals(actual, 6);
        Mockito.verify(spySpark).executorCores();
        Mockito.verify(spySpark).executorInstances();
    }

}
