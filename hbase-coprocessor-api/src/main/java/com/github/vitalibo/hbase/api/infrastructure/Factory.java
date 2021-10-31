package com.github.vitalibo.hbase.api.infrastructure;

import com.github.vitalibo.hbase.api.core.facade.HeatmapFacade;
import com.github.vitalibo.hbase.api.core.facade.PingFacade;
import com.github.vitalibo.hbase.api.core.math.HeatmapRenderer;
import com.github.vitalibo.hbase.api.infrastructure.mock.RandomHeatmapRepository;
import com.github.vitalibo.hbase.api.infrastructure.springframework.HttpRequestMappingHandlerAdapter;
import com.github.vitalibo.hbase.api.infrastructure.springframework.RequestTracingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Factory {

    @Bean
    public PingFacade createPingFacade() {
        return new PingFacade();
    }

    @Bean
    public HeatmapFacade createHeatmapFacade() {
        return new HeatmapFacade(new RandomHeatmapRepository(), new HeatmapRenderer());
    }

    @Bean
    public WebMvcRegistrations createWebMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
                return new HttpRequestMappingHandlerAdapter();
            }
        };
    }

    @Bean
    public FilterRegistrationBean<GenericFilterBean> registerRequestTracingFilter(@Value("${logging.tracing.header}")
                                                                                      String incomingHeader) {
        FilterRegistrationBean<GenericFilterBean> bean = new FilterRegistrationBean<>();
        bean.setFilter(new RequestTracingFilter(incomingHeader));
        bean.setOrder(1);
        return bean;
    }

}
