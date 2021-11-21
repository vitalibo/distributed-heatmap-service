package com.github.vitalibo.heatmap.api.infrastructure;

import com.github.vitalibo.heatmap.api.core.facade.HeatmapFacade;
import com.github.vitalibo.heatmap.api.core.facade.HeatmapJsonFacade;
import com.github.vitalibo.heatmap.api.core.facade.PingFacade;
import com.github.vitalibo.heatmap.api.core.model.HeatmapJsonResponse;
import com.github.vitalibo.heatmap.api.core.model.HeatmapResponse;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpResponse;
import com.github.vitalibo.heatmap.api.core.model.PingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Controller {

    private final PingFacade pingFacade;
    private final HeatmapFacade heatmapFacade;
    private final HeatmapJsonFacade heatmapJsonFacade;

    @GetMapping(value = "ping", produces = "application/json")
    public HttpResponse<PingResponse> ping(HttpRequest request) {
        return pingFacade.process(request);
    }

    @GetMapping(value = "heatmap", consumes = "image/png", produces = "image/png")
    public HttpResponse<HeatmapResponse> heatmap(HttpRequest request) {
        return heatmapFacade.process(request);
    }

    @GetMapping(value = "heatmap", produces = "application/json")
    public HttpResponse<HeatmapJsonResponse> heatmapJson(HttpRequest request) {
        return heatmapJsonFacade.process(request);
    }

}
