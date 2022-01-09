package com.github.vitalibo.heatmap.loader.infrastructure.hbase.transform;

import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.spark.sql.Row;
import scala.Tuple2;

import java.util.Iterator;

@RequiredArgsConstructor
public class RowTranslator {

    private final String tableName;

    public Iterator<Tuple2<ImmutableBytesWritable, Put>> from(Row row) {
        return null;
    }

}
