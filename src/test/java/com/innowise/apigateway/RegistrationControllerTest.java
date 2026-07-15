package com.innowise.apigateway;

import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RegistrationControllerTest extends IntegrationTest {

  @Test
  void shouldRegisterSuccessfully() throws JsonProcessingException {
    stubCreateUser(1L);
    stubSaveCredentials();
    stubLogin("access-token", "refresh-token");

    String request = createRegisterRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 1, 1),
        "login",
        "Password1"
    );

    webTestClient.post()
        .uri("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .jsonPath("$.accessToken").isEqualTo("access-token")
        .jsonPath("$.refreshToken").isEqualTo("refresh-token");
  }

  @Test
  void shouldRollbackUserSuccessfully() throws JsonProcessingException {
    stubCreateUser(1L);
    stubSaveCredentialsFail();
    stubDeleteUser(1L);

    String request = createRegisterRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 1, 1),
        "login",
        "Password1"
    );

    webTestClient.post()
        .uri("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isEqualTo(500);

    verify(WireMock.deleteRequestedFor(WireMock.urlEqualTo("/users/1")));
  }

  @Test
  void shouldBe409WhenUserAlreadyExists() throws JsonProcessingException {
    stubCreateUserConflict();

    String request = createRegisterRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 1, 1),
        "login",
        "Password1"
    );

    webTestClient.post()
        .uri("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isEqualTo(409);
  }

  @Test
  void shouldBe400WhenRequestInvalid() throws JsonProcessingException {
    String request = createRegisterRequest(
        "",
        "Gupanov",
        "gmail.com",
        LocalDate.of(2006, 1, 1),
        "login",
        "password"
    );

    webTestClient.post()
        .uri("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()
        .expectStatus().isBadRequest();
  }
}
