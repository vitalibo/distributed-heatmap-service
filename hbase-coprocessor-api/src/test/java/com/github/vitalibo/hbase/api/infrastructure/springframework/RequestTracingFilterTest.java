package com.github.vitalibo.hbase.api.infrastructure.springframework;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestTracingFilterTest {

    @Mock
    private HttpServletRequest mockHttpServletRequest;
    @Mock
    private HttpServletResponse mockHttpServletResponse;
    @Mock
    private FilterChain mockFilterChain;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testDoFilterInternalWhenHeaderNotSpecified() throws ServletException, IOException {
        RequestTracingFilter filter = new RequestTracingFilter(null);
        String[] requestId = new String[1];
        Mockito.doAnswer(answer -> {
            requestId[0] = MDC.get("Request-Id");
            Assert.assertNotNull(requestId[0]);
            Assert.assertTrue(requestId[0].matches("[\\w]{8}(-[\\w]{4}){3}-[\\w]{12}"));
            return null;
        }).when(mockFilterChain).doFilter(Mockito.any(), Mockito.any());

        Assert.assertNull(MDC.get("Request-Id"));
        filter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        Assert.assertNull(MDC.get("Request-Id"));
        Mockito.verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        Mockito.verify(mockHttpServletResponse).addHeader("Request-Id", requestId[0]);
    }

    @Test
    public void testDoFilterInternalWhenHeaderNotSetInRequest() throws ServletException, IOException {
        RequestTracingFilter filter = new RequestTracingFilter("Incoming-Request-Id");
        String[] requestId = new String[1];
        Mockito.doAnswer(answer -> {
            requestId[0] = MDC.get("Request-Id");
            Assert.assertNotNull(requestId[0]);
            Assert.assertTrue(requestId[0].matches("[\\w]{8}(-[\\w]{4}){3}-[\\w]{12}"));
            return null;
        }).when(mockFilterChain).doFilter(Mockito.any(), Mockito.any());
        Mockito.when(mockHttpServletRequest.getHeader("Incoming-Request-Id")).thenReturn("");

        Assert.assertNull(MDC.get("Request-Id"));
        filter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        Assert.assertNull(MDC.get("Request-Id"));
        Mockito.verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        Mockito.verify(mockHttpServletResponse).addHeader("Request-Id", requestId[0]);
    }

    @Test
    public void testDoFilterInternal() throws ServletException, IOException {
        RequestTracingFilter filter = new RequestTracingFilter("Incoming-Request-Id");
        Mockito.doAnswer(answer -> {
            String requestId = MDC.get("Request-Id");
            Assert.assertNotNull(requestId);
            Assert.assertEquals(requestId, "FooBarBaz");
            return null;
        }).when(mockFilterChain).doFilter(Mockito.any(), Mockito.any());
        Mockito.when(mockHttpServletRequest.getHeader("Incoming-Request-Id"))
            .thenReturn("FooBarBaz");

        Assert.assertNull(MDC.get("Request-Id"));
        filter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        Assert.assertNull(MDC.get("Request-Id"));
        Mockito.verify(mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
        Mockito.verify(mockHttpServletResponse).addHeader("Request-Id", "FooBarBaz");
    }

}
