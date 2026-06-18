package com.innowise.apigateway.filter;

import com.innowise.apigateway.dto.ValidateRequest;
import com.innowise.apigateway.dto.ValidateResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

  private static final List<String> OPEN_PATHS = List.of(
      "/auth/login",
      "/auth/register",
      "/auth/refresh"
  );

  private final WebClient webClient;

  public JwtGlobalFilter(@Qualifier("authServiceWebClient") WebClient webClient) {

    this.webClient = webClient;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();

    if (OPEN_PATHS.stream().anyMatch(path::startsWith)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return unauthorized(exchange);
    }

    String token = authHeader.substring(7);

    return webClient.post()
        .uri("/auth/validate")
        .bodyValue(new ValidateRequest(token))
        .retrieve()
        .bodyToMono(ValidateResponse.class)
        .flatMap(response -> {
          if (!response.isValid()) {
            return unauthorized(exchange);
          }

          ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
              .header("X-User-Id", response.getUserId().toString())
              .header("X-User-Role", response.getRole())
              .build();

          return chain.filter(exchange.mutate().request(mutatedRequest).build());
        })
        .onErrorResume(error -> unauthorized(exchange));
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);

    return exchange.getResponse().setComplete();
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
