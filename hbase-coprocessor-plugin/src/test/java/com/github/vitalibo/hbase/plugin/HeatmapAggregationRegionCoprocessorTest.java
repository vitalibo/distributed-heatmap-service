package com.github.vitalibo.hbase.plugin;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellBuilderType;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.ExtendedCellBuilderFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.coprocessor.RegionObserver;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HeatmapAggregationRegionCoprocessorTest {

    @Mock
    private ObserverContext<RegionCoprocessorEnvironment> mockObserverContext;
    @Mock
    private InternalScanner mockInternalScanner;

    private HeatmapAggregationRegionCoprocessor coprocessor;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        coprocessor = new HeatmapAggregationRegionCoprocessor();
    }

    @Test
    public void testGetRegionObserver() {
        Optional<RegionObserver> actual = coprocessor.getRegionObserver();

        Assert.assertTrue(actual.isPresent());
        Assert.assertSame(actual.get(), coprocessor);
    }

    @Test
    public void testPostScannerNextWhenEmpty() throws IOException {
        List<Result> results = new ArrayList<>();
        boolean actual = coprocessor.postScannerNext(mockObserverContext, mockInternalScanner, results, 123, true);

        Assert.assertTrue(actual);
        Assert.assertTrue(results.isEmpty());
        Mockito.verify(mockInternalScanner, Mockito.never()).next(Mockito.any());
    }

    @Test
    public void testPostScannerNext() throws IOException {
        List<Result> results = new ArrayList<>(Arrays.asList(
            Result.create(Arrays.asList(
                cell("1", 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                cell("2", 0.0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0))),
            Result.create(Collections.singletonList(
                cell("3", 0.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0)))));
        Mockito.doAnswer(ans -> {
            List<Cell> arg = ans.getArgument(0);
            arg.add(cell("4", 0.0, 0.0, 0.0, 4.0, 0.0, 0.0, 0.0));
            arg.add(cell("5", 0.0, 0.0, 0.0, 0.0, 5.0, 0.0, 0.0));
            return true;
        }).doAnswer(ans -> {
            List<Cell> arg = ans.getArgument(0);
            arg.add(cell("6", 0.0, 0.0, 0.0, 0.0, 0.0, 6.0, 0.0));
            return true;
        }).doAnswer(ans -> {
            List<Cell> arg = ans.getArgument(0);
            arg.add(cell("7", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 7.0));
            return false;
        }).doAnswer(ans -> false).when(mockInternalScanner).next(Mockito.anyList());
        boolean actual = coprocessor.postScannerNext(mockObserverContext, mockInternalScanner, results, 123, true);

        Assert.assertTrue(actual);
        Assert.assertFalse(results.isEmpty());
        Assert.assertEquals(results.size(), 1);
        Result result = results.get(0);
        Assert.assertNotNull(result);
        List<Cell> cells = result.listCells();
        Assert.assertEquals(cells.size(), 1);
        Cell cell = cells.get(0);
        Assert.assertEquals(Bytes.toString(CellUtil.cloneRow(cell)), "ROW:1");
        Assert.assertEquals(Bytes.toString(CellUtil.cloneFamily(cell)), "DATA");
        Assert.assertEquals(Bytes.toString(CellUtil.cloneQualifier(cell)), "HM");
        Assert.assertEquals(cell.getTimestamp(), 1234567890L);
        Assert.assertEquals(cell.getType(), Cell.Type.Put);
        Assert.assertEquals(
            Snappy.uncompressDoubleArray(CellUtil.cloneValue(cell)),
            new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0});
    }

    public static Cell cell(String row, double... score) {
        return ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
            .setRow(Bytes.toBytes("ROW:" + row))
            .setFamily(Bytes.toBytes("DATA"))
            .setQualifier(Bytes.toBytes("HM"))
            .setTimestamp(1234567890L)
            .setType(Cell.Type.Put)
            .setValue(Snappy.compress(score))
            .build();
    }

}
