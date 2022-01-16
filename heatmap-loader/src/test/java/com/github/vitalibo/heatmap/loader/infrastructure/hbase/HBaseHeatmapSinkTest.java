package com.github.vitalibo.heatmap.loader.infrastructure.hbase;

import com.github.vitalibo.heatmap.loader.DataFrameSuiteBase;
import com.github.vitalibo.heatmap.loader.TestHelper;
import com.github.vitalibo.heatmap.loader.core.Sink;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.MiniHBaseCluster;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder.ModifyableColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder.ModifyableTableDescriptor;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import scala.Tuple2;
import scala.collection.mutable.WrappedArray;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

public class HBaseHeatmapSinkTest extends DataFrameSuiteBase {

    private MiniZooKeeperCluster zookeeperCluster;
    private MiniHBaseCluster hbaseCluster;
    private Connection connection;
    private Sink<Row> sink;

    @BeforeClass
    public void startHBaseCluster() throws Exception {
        final HBaseTestingUtility utility = new HBaseTestingUtility();
        zookeeperCluster = utility.startMiniZKCluster();
        hbaseCluster = utility.startMiniHBaseCluster();
        hbaseCluster.waitForActiveAndReadyMaster(10_000);
    }

    @BeforeClass(dependsOnMethods = "startHBaseCluster")
    public void setUp() throws IOException {
        Configuration hbaseConf = hbaseCluster.getConfiguration();
        hbaseConf.set("hbase.mapred.outputtable", "sample");
        connection = ConnectionFactory.createConnection(hbaseConf);
        Admin admin = connection.getAdmin();
        ModifyableTableDescriptor descriptor = new ModifyableTableDescriptor(TableName.valueOf("sample"))
            .setColumnFamily(new ModifyableColumnFamilyDescriptor(Bytes.toBytes("foo")));
        admin.createTable(descriptor);
        sink = new HBaseHeatmapSink(hbaseCluster.getConfiguration(), new SumTranslator());
    }

    @Test
    public void testWrite() throws IOException {
        Dataset<Row> df = createDataFrame(
            TestHelper.resourcePath("source.json"),
            TestHelper.resourcePath("source.schema.json"));
        Table table = connection.getTable(TableName.valueOf("sample"));
        class WrappedTable {
            double asDouble(String key) throws IOException {
                Result result = table.get(new Get(Bytes.toBytes(key)));
                return Bytes.toDouble(result.getValue("foo".getBytes(), "bar".getBytes()));
            }
        }

        sink.write(spark(), df);

        WrappedTable wrapped = new WrappedTable();
        Assert.assertEquals(wrapped.asDouble("#1"), 13.5);
        Assert.assertEquals(wrapped.asDouble("#2"), 22.5);
        Assert.assertEquals(wrapped.asDouble("#3"), 31.5);
    }

    @AfterClass
    public void shutdownHBaseCluster() throws IOException {
        connection.close();
        hbaseCluster.shutdown();
        zookeeperCluster.shutdown();
    }

    private static class SumTranslator implements PairFlatMapFunction<Row, ImmutableBytesWritable, Put> {

        @Override
        public Iterator<Tuple2<ImmutableBytesWritable, Put>> call(Row row) {
            return Collections.singletonList(new Tuple2<>(new ImmutableBytesWritable(), fromRow(row)))
                .iterator();
        }

        private static Put fromRow(Row row) {
            WrappedArray<Double> heatmap = row.getAs("heatmap");
            return new Put(Bytes.toBytes((String) row.getAs("name")))
                .addColumn("foo".getBytes(), "bar".getBytes(), Bytes.toBytes(heatmap.reduce(Double::sum)));
        }

    }

}
