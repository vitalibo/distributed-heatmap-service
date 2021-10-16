package com.github.vitalibo.hbase.api.infrastructure;

import com.github.vitalibo.hbase.api.core.facade.PingFacade;
import com.github.vitalibo.hbase.api.infrastructure.springframework.HttpRequestMappingHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Factory {

    @Bean
    public WebMvcRegistrations createWebMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
                return new HttpRequestMappingHandlerAdapter();
            }
        };
    }

}
