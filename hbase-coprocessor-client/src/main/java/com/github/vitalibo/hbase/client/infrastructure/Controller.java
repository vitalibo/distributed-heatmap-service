package com.github.vitalibo.hbase.client.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1", produces = "application/json")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Controller {

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }

}
