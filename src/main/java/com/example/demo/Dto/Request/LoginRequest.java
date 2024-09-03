package com.example.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {
    @NotBlank(message = "email required")
    @Email(message = "wrong email format")
    @Size(max = 50, message = "email max {max} characters")
    private String email;
    @NotNull(message = "password required")
    @Size(min = 6, max = 50, message = "password between {min} and {max} character")
    private String password;
}
