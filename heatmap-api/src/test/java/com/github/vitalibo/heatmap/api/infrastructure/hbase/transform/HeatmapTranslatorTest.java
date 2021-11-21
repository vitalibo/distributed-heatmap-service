package com.github.vitalibo.heatmap.api.infrastructure.hbase.transform;

import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellBuilderType;
import org.apache.hadoop.hbase.ExtendedCellBuilderFactory;
import org.apache.hadoop.hbase.MetaCellComparator;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xerial.snappy.Snappy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HeatmapTranslatorTest {

    @Mock
    private ResultScanner mockResultScanner;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testFrom() {
        Mockito.when(mockResultScanner.spliterator())
            .thenReturn(Arrays.spliterator(new Result[]{
                result("123:1", new double[]{
                    1.0, 0.0, 0.0, 0.0, 1.0,
                    0.0, 1.0, 0.0, 1.0, 0.0,
                    0.0, 0.0, 1.0, 0.0, 0.0
                }),
                result("123:2", new double[]{
                    0.0, 0.0, 2.0, 0.0, 0.0,
                    0.0, 2.0, 0.0, 2.0, 0.0,
                    2.0, 0.0, 0.0, 0.0, 2.0
                })
            }));

        Heatmap actual = HeatmapTranslator.from(
            result("123", 5, 3),
            mockResultScanner);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getWidth(), 5);
        Assert.assertEquals(actual.getHeight(), 3);
        Assert.assertEquals(actual.getScore(), new double[][]{
            {1.0, 0.0, 2.0, 0.0, 1.0},
            {0.0, 3.0, 0.0, 3.0, 0.0},
            {2.0, 0.0, 1.0, 0.0, 2.0}
        });
    }

    public static Result result(String row, double[] score) {
        return Result.create(Collections.singletonList(
            ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
                .setRow(Bytes.toBytes(row))
                .setFamily(Bytes.toBytes("DATA"))
                .setQualifier(Bytes.toBytes("HM"))
                .setTimestamp(1234567890L)
                .setType((byte) 0)
                .setValue(Snappy.compress(score))
                .build()));
    }

    public static Result result(String row, int width, int height) {
        List<Cell> cells = Arrays.asList(
            ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
                .setRow(Bytes.toBytes(row))
                .setFamily(Bytes.toBytes("META"))
                .setQualifier(Bytes.toBytes("W"))
                .setTimestamp(1234567890L)
                .setType((byte) 0)
                .setValue(Bytes.toBytes(width))
                .build(),
            ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
                .setRow(Bytes.toBytes(row))
                .setFamily(Bytes.toBytes("META"))
                .setQualifier(Bytes.toBytes("H"))
                .setTimestamp(1234567890L)
                .setType((byte) 0)
                .setValue(Bytes.toBytes(height))
                .build());
        cells.sort(MetaCellComparator.META_COMPARATOR);
        return Result.create(cells);
    }

}
