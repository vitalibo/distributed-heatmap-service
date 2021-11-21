package com.github.vitalibo.heatmap.api.infrastructure.hbase;

import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRangeQuery;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

public class HBaseRepositoryTest {

    @Mock
    private Table mockTable;
    @Mock
    private BiFunction<Result, ResultScanner, Heatmap> mockTranslator;
    @Mock
    private Result mockResult;
    @Mock
    private ResultScanner mockResultScanner;
    @Captor
    private ArgumentCaptor<Get> captorGet;
    @Captor
    private ArgumentCaptor<Scan> captorScan;

    private HBaseRepository repository;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        repository = new HBaseRepository(mockTable, mockTranslator);
    }

    @Test
    public void testQueryByRange() throws IOException {
        Mockito.when(mockTable.get(Mockito.any(Get.class))).thenReturn(mockResult);
        Mockito.when(mockTable.getScanner(Mockito.any(Scan.class))).thenReturn(mockResultScanner);
        Heatmap heatmap = new Heatmap();
        heatmap.setHeight(480);
        heatmap.setWidth(640);
        Mockito.when(mockTranslator.apply(mockResult, mockResultScanner)).thenReturn(heatmap);
        HeatmapRangeQuery query = new HeatmapRangeQuery();
        query.setId(123L);
        query.setFrom(LocalDateTime.parse("2021-01-01T00:00:00"));
        query.setUnit(LocalDateTime.parse("2021-01-07T00:00:00"));

        Heatmap actual = repository.queryByRange(query);

        Assert.assertEquals(actual, heatmap);
        Mockito.verify(mockTable).get(captorGet.capture());
        Get get = captorGet.getValue();
        Assert.assertEquals(Bytes.toString(get.getRow()), "123");
        Mockito.verify(mockTable).getScanner(captorScan.capture());
        Scan scan = captorScan.getValue();
        Assert.assertEquals(Bytes.toString(scan.getStartRow()), "123:9223372035244798207");
        Assert.assertEquals(Bytes.toString(scan.getStopRow()), "123:9223372035245316607");
        Mockito.verify(mockTranslator).apply(mockResult, mockResultScanner);
    }

}
