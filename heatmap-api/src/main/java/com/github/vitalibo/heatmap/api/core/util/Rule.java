package com.github.vitalibo.heatmap.api.core.util;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface Rule<T> extends BiConsumer<T, ErrorState> {
}
