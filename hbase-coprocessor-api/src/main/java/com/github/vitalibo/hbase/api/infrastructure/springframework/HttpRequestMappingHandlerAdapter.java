package com.github.vitalibo.hbase.api.infrastructure.springframework;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HttpRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        final List<HttpMessageConverter<?>> converters = getMessageConverters();
        converters.add(new HeatmapHttpMessageConverter());

        final HttpRequestResponseEntityMethodProcessor handler =
            new HttpRequestResponseEntityMethodProcessor(
                converters,
                getField("contentNegotiationManager"),
                getField("requestResponseBodyAdvice"));

        List<HandlerMethodArgumentResolver> argumentResolvers = createOrGetModifiedList(getArgumentResolvers());
        argumentResolvers.add(0, handler);
        super.setArgumentResolvers(argumentResolvers);

        List<HandlerMethodReturnValueHandler> returnValueHandlers = createOrGetModifiedList(getReturnValueHandlers());
        returnValueHandlers.add(0, handler);
        setReturnValueHandlers(returnValueHandlers);
    }

    private <T> T getField(String name) {
        final Field field = ReflectionUtils.findField(RequestMappingHandlerAdapter.class, name);
        field.setAccessible(true);
        return (T) ReflectionUtils.getField(field, this);
    }

    private static <T> List<T> createOrGetModifiedList(List<T> handlers) {
        return Optional.ofNullable(handlers)
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);
    }

}
