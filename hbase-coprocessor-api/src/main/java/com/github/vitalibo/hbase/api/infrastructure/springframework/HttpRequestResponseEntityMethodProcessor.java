package com.github.vitalibo.hbase.api.infrastructure.springframework;

import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequestResponseEntityMethodProcessor extends HttpEntityMethodProcessor {

    public HttpRequestResponseEntityMethodProcessor(List<HttpMessageConverter<?>> converters,
                                                    ContentNegotiationManager manager,
                                                    List<Object> requestResponseBodyAdvice) {
        super(converters, manager, requestResponseBodyAdvice);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(HttpRequest.class);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public HttpRequest resolveArgument(MethodParameter parameter,
                                       ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest,
                                       WebDataBinderFactory binderFactory) {
        final HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpRequest request = new HttpRequest();
        if (nativeRequest == null) {
            return request;
        }

        request.setPath(nativeRequest.getServletPath());
        request.setHttpMethod(nativeRequest.getMethod());
        request.setHeaders(Collections.list(nativeRequest.getHeaderNames()).stream()
            .collect(Collectors.toMap(o -> o, nativeRequest::getHeader)));
        request.setQueryStringParameters(nativeRequest.getParameterMap().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, o -> o.getValue()[0])));
        request.setPathParameters((Map<String, String>)
            nativeRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
        request.setBody(nativeRequest.getReader().lines()
            .collect(Collectors.joining(System.lineSeparator())));
        return request;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return HttpResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {
        final HttpResponse<?> response = (HttpResponse<?>) returnValue;
        HttpHeaders headers = new HttpHeaders();
        response.getHeaders().forEach(headers::add);
        ResponseEntity<?> returnResponseEntity = new ResponseEntity<>(
            response.getBody(), headers, HttpStatus.valueOf(response.getStatusCode()));
        super.handleReturnValue(returnResponseEntity, returnType, mavContainer, webRequest);
    }

}
