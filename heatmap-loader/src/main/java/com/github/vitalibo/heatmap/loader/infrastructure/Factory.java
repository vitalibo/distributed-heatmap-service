package com.github.vitalibo.heatmap.loader.infrastructure;

import com.github.vitalibo.config.ConfigFactory;
import com.github.vitalibo.heatmap.loader.core.Heatmap;
import com.github.vitalibo.heatmap.loader.core.Job;
import com.github.vitalibo.heatmap.loader.core.Spark;
import com.github.vitalibo.heatmap.loader.core.io.FileJsonSink;
import com.github.vitalibo.heatmap.loader.core.io.FileJsonSource;
import com.github.vitalibo.heatmap.loader.core.job.GenerateJob;
import com.github.vitalibo.heatmap.loader.core.job.WordCountJob;
import com.github.vitalibo.heatmap.loader.core.math.PerlinNoiseHeatmap;
import com.github.vitalibo.heatmap.loader.core.math.RandomHeatmap;
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
import org.apache.spark.api.java.function.Function0;
import org.apache.spark.sql.SparkSession;
import scala.collection.JavaConverters;

import java.awt.Dimension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                    (accumulator, entry) -> accumulator.set(entry.getKey(), entry.getValue().unwrapped().toString()),
                    (first, second) -> first.setAll(JavaConverters.collectionAsScalaIterable(Arrays.asList(second.getAll()))))))
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

    public Job createWordCountJob(String[] args) {
        return new WordCountJob(
            new FileJsonSource(config.getString("job.word-count.source.path")),
            new FileJsonSink(config.getString("job.word-count.sink.path")));
    }

    public Job createGenerateJob(String[] args) {
        final Configuration hbaseConf = HBaseConfiguration.create();
        hbaseConf.set("hbase.zookeeper.quorum", config.getString("hbase.zookeeper.quorum"));
        hbaseConf.set("hbase.mapred.outputtable", config.getString("hbase.heatmap.table-name"));
        createTableIfNotExists(hbaseConf);

        return new GenerateJob(
            new Dimension(
                config.getInt("job.generate.dimension.width"),
                config.getInt("job.generate.dimension.height")),
            IntStream.range(
                    config.getInt("job.generate.labels.lower"),
                    config.getInt("job.generate.labels.upper"))
                .mapToObj(String::valueOf)
                .collect(Collectors.toList()),
            LocalDateTime.parse(config.getString("job.generate.timerange.lower")),
            LocalDateTime.parse(config.getString("job.generate.timerange.upper")),
            createHeatmapGenerateStrategy(args),
            new HBaseHeatmapSink(
                hbaseConf,
                config.getString("hbase.heatmap.schema.metadata.family"),
                config.getString("hbase.heatmap.schema.data.family"),
                config.getString("hbase.heatmap.schema.metadata.qualifiers.width"),
                config.getString("hbase.heatmap.schema.metadata.qualifiers.height"),
                config.getString("hbase.heatmap.schema.data.qualifiers.heatmap")));
    }

    private Heatmap createHeatmapGenerateStrategy(String[] args) {
        switch (args[1]) {
            case "random":
                return new RandomHeatmap(
                    config.getDouble("job.generate.strategy.random.dense"),
                    config.getDouble("job.generate.strategy.random.min"),
                    config.getDouble("job.generate.strategy.random.max"));
            case "perlin_noise":
                return new PerlinNoiseHeatmap(
                    config.getDouble("job.generate.strategy.perlin_noise.stretch"),
                    config.getDouble("job.generate.strategy.perlin_noise.min"),
                    config.getDouble("job.generate.strategy.perlin_noise.max"),
                    config.getDouble("job.generate.strategy.perlin_noise.pow"));
            default:
                throw new IllegalArgumentException("Unknown heatmap generation strategy name.");
        }
    }

    @SneakyThrows
    void createTableIfNotExists(Configuration configuration) {
        createTableIfNotExists(() -> ConnectionFactory.createConnection(configuration), config);
    }

    static void createTableIfNotExists(Function0<Connection> factory, Config config) throws Exception {
        try (Connection connection = factory.call(); Admin admin = connection.getAdmin()) {
            TableName tableName = TableName.valueOf(config.getString("hbase.heatmap.table-name"));

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
