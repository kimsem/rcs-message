// gateway-service/src/main/java/com/ktds/rcsp/gateway/filter/LoggingFilter.java
package com.ktds.rcsp.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String requestPath = exchange.getRequest().getPath().toString();
        String requestMethod = exchange.getRequest().getMethod().toString();

        log.info("Incoming request {} {}", requestMethod, requestPath);

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    HttpStatus statusCode = (HttpStatus) exchange.getResponse().getStatusCode();
                    log.info("Outgoing response {} {} {} {}ms",
                            requestMethod, requestPath, statusCode, (endTime - startTime));
                }));
    }
}
