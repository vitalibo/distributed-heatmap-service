package com.github.vitalibo.heatmap.api.core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public final class ErrorState extends HashMap<String, Collection<String>> {

    public boolean hasErrors() {
        return !this.isEmpty();
    }

    public ErrorState addError(String field, String message) {
        Collection<String> collection = this.computeIfAbsent(field, o -> new HashSet<>());
        collection.add(message);
        return this;
    }

}
