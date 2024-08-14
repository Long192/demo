package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "refreshToken required")
    private String refreshToken;
}
