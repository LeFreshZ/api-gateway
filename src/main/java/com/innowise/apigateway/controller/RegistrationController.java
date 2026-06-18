package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.CreateUserRequest;
import com.innowise.apigateway.dto.LoginRequest;
import com.innowise.apigateway.dto.RegisterRequest;
import com.innowise.apigateway.dto.SaveCredentialsRequest;
import com.innowise.apigateway.dto.TokensResponse;
import com.innowise.apigateway.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class RegistrationController {

  private static final String DEFAULT_ROLE = "ROLE_USER";

  private final WebClient userServiceWebClient;
  private final WebClient authServiceWebClient;

  public RegistrationController(
      @Qualifier("userServiceWebClient") WebClient userServiceWebClient,
      @Qualifier("authServiceWebClient") WebClient authServiceWebClient
  ) {

    this.userServiceWebClient = userServiceWebClient;
    this.authServiceWebClient = authServiceWebClient;
  }

  @PostMapping("/register")
  public Mono<ResponseEntity<TokensResponse>> register(@Valid @RequestBody RegisterRequest request) {
    CreateUserRequest createUserRequest = new CreateUserRequest(
        request.getName(),
        request.getSurname(),
        request.getEmail(),
        request.getBirthDate()
    );

    return userServiceWebClient.post()
        .uri("/users")
        .bodyValue(createUserRequest)
        .retrieve()
        .bodyToMono(UserResponse.class)
        .flatMap(response -> saveCredentials(request, response.getUserId()))
        .map(tokens -> ResponseEntity.status(201).body(tokens));
  }

  private Mono<TokensResponse> saveCredentials(RegisterRequest request, Long userId) {
    SaveCredentialsRequest credentialsRequest = new SaveCredentialsRequest(
        userId,
        request.getLogin(),
        request.getPassword(),
        DEFAULT_ROLE
    );

    return authServiceWebClient.post()
        .uri("/credentials")
        .bodyValue(credentialsRequest)
        .retrieve()
        .bodyToMono(Void.class)
        .then(login(request))
        .onErrorResume(error -> rollbackUser(userId).then(Mono.error(error)));
  }

  private Mono<Void> rollbackUser(Long userId) {
    return userServiceWebClient.delete()
        .uri("/users/{id}", userId)
        .retrieve()
        .bodyToMono(Void.class)
        .onErrorResume(error -> Mono.empty());
  }

  private Mono<TokensResponse> login(RegisterRequest request) {
    LoginRequest loginRequest = new LoginRequest(request.getLogin(), request.getPassword());

    return authServiceWebClient.post()
        .uri("/auth/login")
        .bodyValue(loginRequest)
        .retrieve()
        .bodyToMono(TokensResponse.class);
  }
}
