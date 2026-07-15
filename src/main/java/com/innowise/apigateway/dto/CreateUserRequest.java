package com.innowise.apigateway.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateUserRequest {

  private String name;
  private String surname;
  private String email;
  private LocalDate birthDate;
}
