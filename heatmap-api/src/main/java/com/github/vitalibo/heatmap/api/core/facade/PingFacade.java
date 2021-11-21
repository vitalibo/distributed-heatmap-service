package com.github.vitalibo.heatmap.api.core.facade;

import com.github.vitalibo.heatmap.api.core.Facade;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpResponse;
import com.github.vitalibo.heatmap.api.core.model.PingRequest;
import com.github.vitalibo.heatmap.api.core.model.PingResponse;
import com.github.vitalibo.heatmap.api.core.model.transform.PingRequestTranslator;
import com.github.vitalibo.heatmap.api.core.util.Rules;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class PingFacade implements Facade<PingRequest, PingResponse> {

    private final Function<HttpRequest, PingRequest> pingRequestTranslator;
    private final Rules<PingRequest> rules;

    public PingFacade() {
        this(PingRequestTranslator::from, new Rules<>());
    }

    @Override
    public HttpResponse<PingResponse> process(HttpRequest request) {
        rules.verify(request);

        PingResponse response = process(
            pingRequestTranslator.apply(request));

        return new HttpResponse<>(response);
    }

    @Override
    public PingResponse process(PingRequest request) {
        rules.verify(request);

        return new PingResponse()
            .withMessage("pong");
    }

}
