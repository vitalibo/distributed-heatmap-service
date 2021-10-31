package com.github.vitalibo.hbase.api.core;

import com.github.vitalibo.hbase.api.core.model.Heatmap;

import java.awt.image.BufferedImage;

public interface Renderer {

    BufferedImage render(Heatmap heatmap, int radius, double opacity);

}
