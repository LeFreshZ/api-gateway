package com.innowise.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SaveCredentialsRequest {

  private Long userId;
  private String login;
  private String password;
  private String role;
}
