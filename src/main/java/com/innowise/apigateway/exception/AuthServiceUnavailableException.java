package com.innowise.apigateway.exception;

public class AuthServiceUnavailableException extends RuntimeException {

  public AuthServiceUnavailableException(String message) {
    super(message);
  }

  public AuthServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
