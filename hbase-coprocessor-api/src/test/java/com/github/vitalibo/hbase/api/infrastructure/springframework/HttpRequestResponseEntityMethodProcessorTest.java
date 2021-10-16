package com.github.vitalibo.hbase.api.infrastructure.springframework;

import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;

public class HttpRequestResponseEntityMethodProcessorTest {

    @Mock
    private MethodParameter mockMethodParameter;
    @Mock
    private ModelAndViewContainer mockModelAndViewContainer;
    @Mock
    private NativeWebRequest mockNativeWebRequest;
    @Mock
    private WebDataBinderFactory mockWebDataBinderFactory;
    @Mock
    private HttpServletRequest mockHttpServletRequest;

    private MockHttpServletResponse servletResponse;
    private ModelAndViewContainer mavContainer;
    private ServletWebRequest webRequest;
    private HttpRequestResponseEntityMethodProcessor processor;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("POST");
        mavContainer = new ModelAndViewContainer();
        servletResponse = new MockHttpServletResponse();
        webRequest = new ServletWebRequest(servletRequest, servletResponse);
        processor = new HttpRequestResponseEntityMethodProcessor(
            converters, new ContentNegotiationManager(), Collections.emptyList());
    }

    @Test
    public void testSupportsParameter() throws Exception {
        Method method = getClass().getDeclaredMethod("handle", HttpRequest.class);
        MethodParameter returnType = new MethodParameter(method, 0);

        boolean actual = processor.supportsParameter(returnType);

        Assert.assertTrue(actual);
    }

    @Test
    public void testUnSupportsParameter() throws Exception {
        Method method = getClass().getDeclaredMethod("handle", String.class);
        MethodParameter returnType = new MethodParameter(method, 0);

        boolean actual = processor.supportsParameter(returnType);

        Assert.assertFalse(actual);
    }

    @Test
    public void testResolveArgument() throws IOException {
        Mockito.when(mockNativeWebRequest.getNativeRequest(HttpServletRequest.class))
            .thenReturn(mockHttpServletRequest);
        Mockito.when(mockHttpServletRequest.getServletPath()).thenReturn("/v1/api");
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn("POST");
        Mockito.when(mockHttpServletRequest.getHeaderNames()).thenReturn(
            Collections.enumeration(Arrays.asList("User-Agent", "Content-Type")));
        Mockito.when(mockHttpServletRequest.getHeader("User-Agent")).thenReturn("MockAgent/1.23");
        Mockito.when(mockHttpServletRequest.getHeader("Content-Type")).thenReturn("application/json");
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("foo", new String[]{"1", "2"});
        parameterMap.put("bar", new String[]{"baz"});
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(parameterMap);
        Mockito.when(mockHttpServletRequest.getAttribute(Mockito.any())).thenReturn(
            Collections.singletonMap("var1", "var2"));
        Mockito.when(mockHttpServletRequest.getReader()).thenReturn(
            new BufferedReader(new StringReader("{\"key\":\"value\"}")));

        HttpRequest actual = processor.resolveArgument(
            mockMethodParameter, mockModelAndViewContainer, mockNativeWebRequest, mockWebDataBinderFactory);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getPath(), "/v1/api");
        Assert.assertEquals(actual.getHttpMethod(), "POST");
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "MockAgent/1.23");
        headers.put("Content-Type", "application/json");
        Assert.assertEquals(actual.getHeaders(), headers);
        Map<String, String> queryStringParameters = new HashMap<>();
        queryStringParameters.put("foo", "1");
        queryStringParameters.put("bar", "baz");
        Assert.assertEquals(actual.getQueryStringParameters(), queryStringParameters);
        Mockito.verify(mockHttpServletRequest).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Assert.assertEquals(actual.getPathParameters(), Collections.singletonMap("var1", "var2"));
        Assert.assertEquals(actual.getBody(), "{\"key\":\"value\"}");
    }

    @Test
    public void testResolveArgumentNativeRequestIsNull() {
        HttpRequest actual = processor.resolveArgument(
            mockMethodParameter, mockModelAndViewContainer, mockNativeWebRequest, mockWebDataBinderFactory);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual, new HttpRequest());
    }

    @Test
    public void testSupportsReturnType() throws Exception {
        Method method = getClass().getDeclaredMethod("handle");
        MethodParameter returnType = new MethodParameter(method, -1);

        boolean actual = processor.supportsReturnType(returnType);

        Assert.assertTrue(actual);
    }

    @Test
    public void testSupportsReturnTypeResponseEntity() throws Exception {
        Method method = getClass().getDeclaredMethod("handleResponseEntity");
        MethodParameter returnType = new MethodParameter(method, -1);

        boolean actual = processor.supportsReturnType(returnType);

        Assert.assertFalse(actual);
    }

    @Test
    public void testHandleReturnValue() throws Exception {
        Method method = getClass().getDeclaredMethod("handle");
        MethodParameter returnType = new MethodParameter(method, -1);
        HttpResponse<String> response = new HttpResponse<>(200, "Foo")
            .withHeaders(Collections.singletonMap("foo", "bar"));

        processor.handleReturnValue(response, returnType, mavContainer, webRequest);

        Assert.assertEquals(servletResponse.getHeader("Content-Type"), "text/plain;charset=ISO-8859-1");
        Assert.assertEquals(servletResponse.getContentAsString(), "Foo");
        Assert.assertEquals(servletResponse.getHeader("foo"), "bar");
    }

    private HttpResponse<String> handle() {
        return null;
    }

    private HttpResponse<String> handle(HttpRequest ignored) {
        return null;
    }

    private HttpResponse<String> handle(String ignored) {
        return null;
    }

    private ResponseEntity<String> handleResponseEntity() {
        return null;
    }

}
