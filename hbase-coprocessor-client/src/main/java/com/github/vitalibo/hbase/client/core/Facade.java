package com.github.vitalibo.hbase.client.core;

import com.github.vitalibo.hbase.client.core.model.HttpRequest;
import com.github.vitalibo.hbase.client.core.model.HttpResponse;

public interface Facade<Request, Response> {

    default HttpResponse<Response> process(HttpRequest request) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    Response process(Request request);

}
