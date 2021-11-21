package com.github.vitalibo.heatmap.api.core;

import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRangeQuery;

public interface Repository {

    Heatmap queryByRange(HeatmapRangeQuery query);

}
