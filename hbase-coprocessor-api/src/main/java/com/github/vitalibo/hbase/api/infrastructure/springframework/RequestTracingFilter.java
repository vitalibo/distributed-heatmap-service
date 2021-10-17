package com.github.vitalibo.hbase.api.infrastructure.springframework;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class RequestTracingFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "Request-Id";
    private final String header;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestId = Optional.ofNullable(header)
            .map(request::getHeader)
            .filter(o -> !o.isEmpty())
            .orElseGet(() -> UUID.randomUUID().toString());

        try {
            MDC.put(MDC_KEY, requestId);
            response.addHeader(MDC_KEY, requestId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

}
