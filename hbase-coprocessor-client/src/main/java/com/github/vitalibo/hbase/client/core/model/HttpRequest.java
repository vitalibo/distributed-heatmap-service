package com.github.vitalibo.hbase.client.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Map;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class HttpRequest {

    private String path;
    private String httpMethod;
    private Map<String, String> headers;
    private Map<String, String> queryStringParameters;
    private Map<String, String> pathParameters;
    private String body;

}
