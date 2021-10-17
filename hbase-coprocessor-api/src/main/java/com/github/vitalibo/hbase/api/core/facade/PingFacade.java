package com.github.vitalibo.hbase.api.core.facade;

import com.github.vitalibo.hbase.api.core.Facade;
import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import com.github.vitalibo.hbase.api.core.model.PingRequest;
import com.github.vitalibo.hbase.api.core.model.PingResponse;
import com.github.vitalibo.hbase.api.core.model.transform.PingRequestTranslator;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class PingFacade implements Facade<PingRequest, PingResponse> {

    private final Function<HttpRequest, PingRequest> pingRequestTranslator;

    public PingFacade() {
        this(PingRequestTranslator::from);
    }

    @Override
    public HttpResponse<PingResponse> process(HttpRequest request) {
        PingResponse response = process(
            pingRequestTranslator.apply(request));

        return new HttpResponse<>(response);
    }

    @Override
    public PingResponse process(PingRequest request) {
        return new PingResponse()
            .withMessage("pong");
    }

}
