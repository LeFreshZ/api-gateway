package com.innowise.apigateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.apigateway.dto.LoginRequest;
import com.innowise.apigateway.dto.RegisterRequest;
import com.innowise.apigateway.dto.TokensResponse;
import com.innowise.apigateway.dto.UserResponse;
import com.innowise.apigateway.dto.ValidateResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8089)
public abstract class IntegrationTest {

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("user-service.url", () -> "http://localhost:8089");
    registry.add("auth-service.url", () -> "http://localhost:8089");

    registry.add("AUTH_SERVICE_URL", () -> "http://localhost:8089");
    registry.add("USER_SERVICE_URL", () -> "http://localhost:8089");
    registry.add("ORDER_SERVICE_URL", () -> "http://localhost:8089");
    registry.add("PAYMENT_SERVICE_URL", () -> "http://localhost:8089");

    registry.add("internal.secret", () -> "testSecret");
  }

  @Autowired
  protected ObjectMapper mapper;

  @Autowired
  protected WebTestClient webTestClient;

  @BeforeEach
  void resetWireMock() {
    WireMock.reset();
  }

  protected void stubValidateToken(boolean valid, Long userId, String role)
      throws JsonProcessingException {

    ValidateResponse response = new ValidateResponse();
    response.setValid(valid);
    response.setUserId(userId);
    response.setRole(role);

    stubFor(post(urlEqualTo("/auth/validate"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(response))));
  }

  protected void stubValidateTokenUnavailable() {
    stubFor(post(urlEqualTo("/auth/validate"))
        .willReturn(aResponse()
            .withStatus(500)));
  }

  protected void stubCreateUser(Long userId) throws JsonProcessingException {
    UserResponse response = new UserResponse();
    response.setUserId(userId);

    stubFor(post(urlEqualTo("/users"))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(response))));
  }

  protected void stubCreateUserConflict() {
    stubFor(post(urlEqualTo("/users"))
        .willReturn(aResponse()
            .withStatus(409)));
  }

  protected void stubDeleteUser(Long userId) {
    stubFor(delete(urlEqualTo("/users/" + userId))
        .willReturn(aResponse()
            .withStatus(204)));
  }

  protected void stubSaveCredentials() {
    stubFor(post(urlEqualTo("/credentials"))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody("{}")));
  }

  protected void stubSaveCredentialsFail() {
    stubFor(post(urlEqualTo("/credentials"))
        .willReturn(aResponse()
            .withStatus(500)));
  }

  protected void stubLogin(String accessToken, String refreshToken) throws JsonProcessingException {
    TokensResponse response = new TokensResponse();
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);

    stubFor(post(urlEqualTo("/auth/login"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(mapper.writeValueAsString(response))));
  }

  protected String createRegisterRequest(
      String name,
      String surname,
      String email,
      LocalDate birthDate,
      String login,
      String password
  ) throws JsonProcessingException {

    RegisterRequest request = new RegisterRequest();

    request.setName(name);
    request.setSurname(surname);
    request.setEmail(email);
    request.setBirthDate(birthDate);
    request.setLogin(login);
    request.setPassword(password);

    return mapper.writeValueAsString(request);
  }

  protected String createLoginRequest(String login, String password)
      throws JsonProcessingException {

    LoginRequest request = new LoginRequest(login, password);

    return mapper.writeValueAsString(request);
  }
}
