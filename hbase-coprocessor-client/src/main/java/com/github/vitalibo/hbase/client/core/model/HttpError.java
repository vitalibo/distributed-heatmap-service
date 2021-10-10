package com.github.vitalibo.hbase.client.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Collection;
import java.util.Map;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpError {

    private Integer status;
    private String message;
    private Map<String, Collection<String>> errors;
    private String requestId;

}
