package com.github.vitalibo.heatmap.api.core;

import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpResponse;

public interface Facade<Request, Response> {

    default HttpResponse<Response> process(HttpRequest request) {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    Response process(Request request);

}
