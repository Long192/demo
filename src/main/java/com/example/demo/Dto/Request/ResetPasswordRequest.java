package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "password required")
    @Size(min = 6, message = "password need atleast 6 character")
    private String password;
}
