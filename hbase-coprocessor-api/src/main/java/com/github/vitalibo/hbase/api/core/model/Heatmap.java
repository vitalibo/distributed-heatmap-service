package com.github.vitalibo.hbase.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Heatmap {

    private int width;
    private int height;
    private double[][] score;

}
