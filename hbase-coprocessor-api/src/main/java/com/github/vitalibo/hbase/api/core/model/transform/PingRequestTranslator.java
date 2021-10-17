package com.github.vitalibo.hbase.api.core.model.transform;

import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.PingRequest;

public final class PingRequestTranslator {

    private PingRequestTranslator() {
    }

    public static PingRequest from(HttpRequest request) {
        return new PingRequest();
    }

}
