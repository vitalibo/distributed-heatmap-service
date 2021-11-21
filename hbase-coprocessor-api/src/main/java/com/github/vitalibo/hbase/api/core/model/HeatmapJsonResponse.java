package com.github.vitalibo.hbase.api.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapJsonResponse {

    @JsonProperty("width")
    private int width;

    @JsonProperty("height")
    private int height;

    @JsonProperty("sparse")
    private double[][] sparse;

    @JsonProperty("dense")
    private double[][] dense;

}
