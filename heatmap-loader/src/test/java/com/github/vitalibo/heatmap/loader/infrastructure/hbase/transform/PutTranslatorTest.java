package com.github.vitalibo.heatmap.loader.infrastructure.hbase.transform;

import com.github.vitalibo.heatmap.loader.TestHelper;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xerial.snappy.Snappy;
import scala.Tuple2;
import scala.collection.mutable.WrappedArray;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class PutTranslatorTest {

    private Set<String> holder;
    private PutTranslator translator;

    @BeforeMethod
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        holder = new HashSet<>();
        translator = new PutTranslator(
            Bytes.toBytes("META"), Bytes.toBytes("DATA"),
            Bytes.toBytes("W"), Bytes.toBytes("H"), Bytes.toBytes("HM"),
            holder);
    }

    @Test
    public void testFromFull() throws IOException {
        Row row = createRow("foo", 640, 480, 1643673600L, 1.2, 3.4, 5.6, 7.8, 9.0);

        List<WrappedPut> list = list(translator.from(row));

        Assert.assertEquals(list.size(), 2);
        WrappedPut actual = list.get(0);
        Assert.assertEquals(actual.getRow(), "foo");
        Assert.assertEquals(Bytes.toInt(actual.getValue("META", "W")), 640);
        Assert.assertEquals(Bytes.toInt(actual.getValue("META", "H")), 480);
        actual = list.get(1);
        Assert.assertEquals(actual.getRow(), "foo:9223372035211102207");
        Assert.assertEquals(Snappy.uncompressDoubleArray(actual.getValue("DATA", "HM")),
            new double[]{1.2, 3.4, 5.6, 7.8, 9.0});
        Assert.assertTrue(holder.contains("foo"));
    }

    @Test
    public void testFrom() throws IOException {
        holder.add("bar");
        Row row = createRow("bar", 480, 640, 1640995200L, 1.2, 3.4, 5.6);

        List<WrappedPut> list = list(translator.from(row));

        Assert.assertEquals(list.size(), 1);
        WrappedPut actual = list.get(0);
        Assert.assertEquals(actual.getRow(), "bar:9223372035213780607");
        Assert.assertEquals(Snappy.uncompressDoubleArray(actual.getValue("DATA", "HM")), new double[]{1.2, 3.4, 5.6});
    }

    private static Row createRow(String id, int width, int height, long timestamp, double... values) {
        return new GenericRowWithSchema(
            new Object[]{
                id, width, height, Timestamp.from(Instant.ofEpochSecond(timestamp)), new WrappedArray.ofDouble(values)},
            TestHelper.resourceAsStructType(TestHelper.resourcePath("../row.schema.json")));
    }

    private static List<WrappedPut> list(Iterator<Tuple2<ImmutableBytesWritable, Put>> iterator) {
        List<WrappedPut> list = new ArrayList<>();
        iterator.forEachRemaining(o -> list.add(new WrappedPut(o)));
        return list;
    }

    @RequiredArgsConstructor
    private static class WrappedPut {

        private final Tuple2<ImmutableBytesWritable, Put> tuple;

        public String getRow() {
            return Bytes.toString(tuple._2.getRow());
        }

        public byte[] getValue(String family, String qualifier) {
            List<Cell> cells = tuple._2.get(Bytes.toBytes(family), Bytes.toBytes(qualifier));
            return CellUtil.cloneValue(cells.get(0));
        }

    }

}
