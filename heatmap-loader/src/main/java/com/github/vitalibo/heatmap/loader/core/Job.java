package com.github.vitalibo.heatmap.loader.core;

@FunctionalInterface
public interface Job {

    void process(Spark spark);

}
