package com.innowise.apigateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class JwtGlobalFilterTest extends IntegrationTest {

  @Test
  void shouldPassRequestWithValidToken() throws JsonProcessingException {
    stubValidateToken(true, 1L, "ROLE_USER");

    stubFor(get(urlEqualTo("/users/1"))
        .willReturn(aResponse()
            .withStatus(200)));

    webTestClient.get()
        .uri("/users/1")
        .header("Authorization", "Bearer valid-token")
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void shouldBe401WhenNoAuthorizationHeader() {
    webTestClient.get()
        .uri("/users/1")
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void shouldBe401WhenInvalidToken() {
    webTestClient.get()
        .uri("/users/1")
        .header("Authorization", "wrong token")
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void shouldBe401WhenAuthServiceUnavailable() {
    stubValidateTokenUnavailable();

    webTestClient.get()
        .uri("/users/1")
        .header("Authorization", "Bearer some-token")
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  void shouldPassLoginWithoutToken() throws JsonProcessingException {
    String request = createLoginRequest("test", "test");

    stubFor(post(urlEqualTo("/auth/login"))
        .willReturn(aResponse()
            .withStatus(200)));

    webTestClient.post()
        .uri("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isOk();
  }

  @Test
  void shouldPassRefreshWithValidToken() throws JsonProcessingException {
    stubValidateToken(true, 1L, "ROLE_USER");

    stubFor(post(urlEqualTo("/auth/refresh"))
        .willReturn(aResponse()
            .withStatus(200)));

    webTestClient.post()
        .uri("/auth/refresh")
        .header("Authorization", "Bearer valid-token")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("{\"refreshToken\":\"token\"}")
        .exchange()
        .expectStatus().isOk();
  }
}
