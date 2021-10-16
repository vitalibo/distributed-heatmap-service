package com.github.vitalibo.hbase.api.infrastructure;

import com.github.vitalibo.hbase.api.infrastructure.springframework.HttpRequestMappingHandlerAdapter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FactoryTest {

    private Factory factory;

    @BeforeMethod
    public void setUp() {
        factory = new Factory();
    }

    @Test
    public void testCreateWebMvcRegistrations() {
        WebMvcRegistrations actual = factory.createWebMvcRegistrations();

        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.getRequestMappingHandlerAdapter() instanceof HttpRequestMappingHandlerAdapter);
    }

}
