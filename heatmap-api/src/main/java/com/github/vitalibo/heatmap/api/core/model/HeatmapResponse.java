package com.github.vitalibo.heatmap.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.awt.image.BufferedImage;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapResponse {

    private BufferedImage canvas;

}
