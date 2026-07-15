package com.innowise.apigateway.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private LocalDateTime time;
  private int status;
  private String message;
}
