package com.github.vitalibo.hbase.api.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public final class ErrorState extends HashMap<String, Collection<String>> {

    public boolean hasErrors() {
        return !this.isEmpty();
    }

    public ErrorState addError(String field, String message) {
        Collection<String> collection = this.computeIfAbsent(field, o -> new ArrayList<>());
        collection.add(message);
        return this;
    }

}
