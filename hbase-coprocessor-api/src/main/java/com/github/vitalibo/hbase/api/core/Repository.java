package com.github.vitalibo.hbase.api.core;

import com.github.vitalibo.hbase.api.core.model.Heatmap;
import com.github.vitalibo.hbase.api.core.model.HeatmapRangeQuery;

public interface Repository {

    Heatmap queryByRange(HeatmapRangeQuery query);

}
