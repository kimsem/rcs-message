// gateway-service/src/main/java/com/ktds/rcsp/gateway/filter/AuthorizationHeaderFilter.java
package com.ktds.rcsp.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public AuthorizationHeaderFilter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Data
    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String token = authorizationHeader.replace("Bearer ", "");

            return webClientBuilder.build()
                    .get()
                    .uri("http://dg4us-rcs-auth:8080/api/auth/verify?token=" + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(response -> {
                        try {
                            JsonNode root = objectMapper.readTree(response);
                            JsonNode data = root.get("data");
                            if (data != null && data.asBoolean()) {
                                return chain.filter(exchange);
                            } else {
                                return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                            }
                        } catch (Exception e) {
                            log.error("Error parsing auth response", e);
                            return onError(exchange, "Error validating token", HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Error calling auth service", e);
                        return onError(exchange, "Error validating token", HttpStatus.INTERNAL_SERVER_ERROR);
                    });
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorMessage = String.format("{\"message\": \"%s\"}", err);
        DataBuffer buffer = response.bufferFactory().wrap(errorMessage.getBytes());

        return response.writeWith(Mono.just(buffer));
    }
}