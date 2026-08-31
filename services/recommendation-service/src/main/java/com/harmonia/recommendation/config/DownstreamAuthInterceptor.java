package com.harmonia.recommendation.config;

import com.harmonia.common.api.http.Headers;
import com.harmonia.common.web.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;

public class DownstreamAuthInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            request.getHeaders().setBearerAuth(jwt.getTokenValue());
        }
        String traceId = MDC.get(CorrelationIdFilter.TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            request.getHeaders().set(Headers.CORRELATION_ID, traceId);
        }
        String requestId = MDC.get(CorrelationIdFilter.REQUEST_ID);
        if (requestId != null && !requestId.isBlank()) {
            request.getHeaders().set(Headers.REQUEST_ID, requestId);
        }
        return execution.execute(request, body);
    }
}
