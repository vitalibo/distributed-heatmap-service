package com.github.vitalibo.heatmap.loader.infrastructure.hbase.transform;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.sql.Row;
import org.xerial.snappy.Snappy;
import scala.Tuple2;
import scala.collection.mutable.WrappedArray;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class PutTranslator implements Serializable {

    private final byte[] familyMeta;
    private final byte[] familyData;
    private final byte[] qualifierWidth;
    private final byte[] qualifierHeight;
    private final byte[] qualifierHeatmap;

    private final Set<String> holder;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public PutTranslator(String familyMeta,
                         String familyData, String qualifierWidth,
                         String qualifierHeight, String qualifierHeatmap) {
        this(Bytes.toBytes(familyMeta), Bytes.toBytes(familyData), Bytes.toBytes(qualifierWidth),
            Bytes.toBytes(qualifierHeight), Bytes.toBytes(qualifierHeatmap),
            new HashSet<>());
    }

    public Iterator<Tuple2<ImmutableBytesWritable, Put>> from(Row row) {
        return fromRow(row)
            .map(o -> new Tuple2<>(new ImmutableBytesWritable(), o))
            .iterator();
    }

    private Stream<Put> fromRow(Row row) {
        String id = row.getAs("id");
        if (holder.add(id)) {
            return Stream.of(
                makeMetaPut(id, row),
                makeDataPut(id, row));
        }

        return Stream.of(
            makeDataPut(id, row));
    }

    private Put makeMetaPut(String id, Row row) {
        return new Put(Bytes.toBytes(id))
            .addColumn(familyMeta, qualifierWidth, Bytes.toBytes((int) row.getAs("width")))
            .addColumn(familyMeta, qualifierHeight, Bytes.toBytes((int) row.getAs("height")));
    }

    private Put makeDataPut(String id, Row row) {
        long timestamp = Long.MAX_VALUE - (((Timestamp) row.getAs("timestamp")).getTime() / (long) 1E+3);
        return new Put(Bytes.toBytes(String.format("%s:%s", id, timestamp)))
            .addColumn(familyData, qualifierHeatmap, compressDoubleArray(row.getAs("heatmap")));
    }

    @SneakyThrows
    private static byte[] compressDoubleArray(WrappedArray<Double> array) {
        int size = array.size();
        double[] doubles = new double[size];
        for (int i = 0; i < size; i++) {
            doubles[i] = array.apply(i);
        }

        return Snappy.compress(doubles);
    }

}
