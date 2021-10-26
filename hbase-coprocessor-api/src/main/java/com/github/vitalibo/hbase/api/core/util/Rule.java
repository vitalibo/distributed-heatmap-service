package com.github.vitalibo.hbase.api.core.util;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface Rule<T> extends BiConsumer<T, ErrorState> {
}
