package com.harmonia.gateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String REQUEST_ID = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = headerOrNew(exchange, CORRELATION_ID);
        String requestId = headerOrNew(exchange, REQUEST_ID);
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(CORRELATION_ID, correlationId)
                .header(REQUEST_ID, requestId)
                .build();
        exchange.getResponse().getHeaders().set(CORRELATION_ID, correlationId);
        exchange.getResponse().getHeaders().set(REQUEST_ID, requestId);
        MDC.put("traceId", correlationId);
        MDC.put("requestId", requestId);
        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(sig -> {
                    MDC.remove("traceId");
                    MDC.remove("requestId");
                });
    }

    private String headerOrNew(ServerWebExchange exchange, String name) {
        String value = exchange.getRequest().getHeaders().getFirst(name);
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
