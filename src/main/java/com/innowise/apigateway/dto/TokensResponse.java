package com.innowise.apigateway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokensResponse {

  private String accessToken;
  private String refreshToken;
}
