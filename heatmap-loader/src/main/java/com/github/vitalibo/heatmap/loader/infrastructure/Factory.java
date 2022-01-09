package com.github.vitalibo.heatmap.loader.infrastructure;

import com.github.vitalibo.config.ConfigFactory;
import com.github.vitalibo.heatmap.loader.core.Job;
import com.github.vitalibo.heatmap.loader.core.Spark;
import com.github.vitalibo.heatmap.loader.core.job.GenerateJob;
import com.github.vitalibo.heatmap.loader.infrastructure.hbase.HBaseHeatmapSink;
import com.typesafe.config.Config;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder.ModifyableColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder.ModifyableTableDescriptor;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import scala.collection.JavaConverters;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collector;

public class Factory {

    @Getter(lazy = true)
    private static final Factory instance = new Factory(
        ConfigFactory.parseResources("application.yaml"),
        ConfigFactory.load("application-default.yaml"));

    private final Config config;

    Factory(Config... configs) {
        this.config = Arrays.stream(configs)
            .reduce(Config::withFallback)
            .orElseThrow(IllegalStateException::new)
            .resolve();
    }

    public Spark createSpark() {
        final SparkSession.Builder builder = SparkSession.builder()
            .config(config.getObject("spark.config")
                .entrySet().stream()
                .collect(Collector.of(
                    SparkConf::new,
                    (accumulator, entry) -> accumulator.set(entry.getKey(), "" + entry.getValue().unwrapped()),
                    (first, second) -> first.setAll(JavaConverters.collectionAsScalaIterable(Arrays.asList((second.getAll())))))))
            .enableHiveSupport();

        final String name = config.getString("spark.app");
        if (Objects.nonNull(name)) {
            builder.appName(name);
        }

        final String master = config.getString("spark.master");
        if (Objects.nonNull(master) && master.startsWith("local")) {
            builder.master(master);
        }

        return new Spark(builder.getOrCreate());
    }


    public Job createGenerateJob(String[] args) {
        final Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.set("hbase.zookeeper.quorum", config.getString("hbase.zookeeper.quorum"));
        final String tableName = config.getString("hbase.heatmap.table-name");
        createTableIfNotExists(hbaseConf, tableName);

        return new GenerateJob(
            new HBaseHeatmapSink(hbaseConf, tableName));
    }

    @SneakyThrows
    void createTableIfNotExists(Configuration configuration, String tableName) {
        createTableIfNotExists(configuration, TableName.valueOf(tableName));
    }

    private void createTableIfNotExists(Configuration configuration, TableName tableName) throws Exception {
        try (Connection connection = ConnectionFactory.createConnection(configuration)) {
            final Admin admin = connection.getAdmin();

            if (admin.tableExists(tableName)) {
                return;
            }

            final ModifyableTableDescriptor descriptor = new ModifyableTableDescriptor(tableName)
                .setCoprocessor(config.getString("hbase.heatmap.coprocessor"));

            for (String family : config.getStringList("hbase.heatmap.column-families")) {
                descriptor.setColumnFamily(
                    new ModifyableColumnFamilyDescriptor(Bytes.toBytes(family)));
            }

            admin.createTable(descriptor);
        }
    }

}
