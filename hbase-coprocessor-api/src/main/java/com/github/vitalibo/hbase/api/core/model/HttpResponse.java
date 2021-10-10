package com.github.vitalibo.hbase.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.HashMap;
import java.util.Map;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class HttpResponse<T> {

    private int statusCode;
    private Map<String, String> headers;
    private T body;

    public HttpResponse(T body) {
        this(200, body);
    }

    public HttpResponse(Integer statusCode, T body) {
        this(statusCode, new HashMap<>(), body);
    }

}

