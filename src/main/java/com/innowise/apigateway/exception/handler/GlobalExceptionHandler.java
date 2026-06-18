package com.innowise.apigateway.exception.handler;

import com.innowise.apigateway.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ErrorResponse> handleValidation(WebExchangeBindException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(", "));

    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 400, message);

    return ResponseEntity.status(400).body(response);
  }

  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<ErrorResponse> handleWebClientError(WebClientResponseException ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), ex.getStatusCode().value(), ex.getMessage());

    return ResponseEntity.status(ex.getStatusCode()).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    ErrorResponse response = new ErrorResponse(LocalDateTime.now(), 500, "Internal server error");

    return ResponseEntity.status(500).body(response);
  }
}
