package com.github.vitalibo.hbase.api.infrastructure;

import com.github.vitalibo.hbase.api.core.facade.PingFacade;
import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import com.github.vitalibo.hbase.api.core.model.PingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1", produces = "application/json")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Controller {

    private final PingFacade pingFacade;

    @GetMapping("ping")
    public HttpResponse<PingResponse> ping(HttpRequest request) {
        return pingFacade.process(request);
    }

}
