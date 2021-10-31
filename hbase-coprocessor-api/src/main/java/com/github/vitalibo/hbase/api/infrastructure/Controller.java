package com.github.vitalibo.hbase.api.infrastructure;

import com.github.vitalibo.hbase.api.core.facade.HeatmapFacade;
import com.github.vitalibo.hbase.api.core.facade.PingFacade;
import com.github.vitalibo.hbase.api.core.model.HeatmapResponse;
import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import com.github.vitalibo.hbase.api.core.model.PingResponse;
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

    @GetMapping(value = "ping", produces = "application/json")
    public HttpResponse<PingResponse> ping(HttpRequest request) {
        return pingFacade.process(request);
    }

    @GetMapping(value = "heatmap", produces = "image/png")
    public HttpResponse<HeatmapResponse> heatmap(HttpRequest request) {
        return heatmapFacade.process(request);
    }

}
