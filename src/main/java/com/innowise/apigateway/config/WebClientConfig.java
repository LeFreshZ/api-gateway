package com.innowise.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient authServiceWebClient(
      WebClient.Builder builder,
      @Value("${auth-service.url}") String authServiceUrl) {

        return builder.baseUrl(authServiceUrl).build();
  }

  @Bean
  public WebClient userServiceWebClient(
      WebClient.Builder builder,
      @Value("${user-service.url}") String userServiceUrl) {

        return builder.baseUrl(userServiceUrl).build();
  }
}
