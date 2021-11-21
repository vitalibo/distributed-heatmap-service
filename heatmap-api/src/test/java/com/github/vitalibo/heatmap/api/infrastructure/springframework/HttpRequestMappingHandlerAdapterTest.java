package com.github.vitalibo.heatmap.api.infrastructure.springframework;

import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class HttpRequestMappingHandlerAdapterTest {

    @Test
    public void testAfterPropertiesSet() {
        HttpRequestMappingHandlerAdapter adapter = new HttpRequestMappingHandlerAdapter();
        adapter.setApplicationContext(new StaticWebApplicationContext());

        adapter.afterPropertiesSet();

        List<HandlerMethodArgumentResolver> argumentResolvers = adapter.getArgumentResolvers();
        Assert.assertNotNull(argumentResolvers);
        Assert.assertTrue(argumentResolvers.size() > 0);
        Assert.assertTrue(argumentResolvers.get(0) instanceof HttpRequestResponseEntityMethodProcessor);

        List<HandlerMethodReturnValueHandler> returnValueHandlers = adapter.getReturnValueHandlers();
        Assert.assertNotNull(returnValueHandlers);
        Assert.assertTrue(returnValueHandlers.size() > 0);
        Assert.assertTrue(returnValueHandlers.get(0) instanceof HttpRequestResponseEntityMethodProcessor);
    }

}
