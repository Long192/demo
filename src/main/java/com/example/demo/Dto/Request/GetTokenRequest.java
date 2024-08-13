package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GetTokenRequest {
    @Size(min = 4, max = 4, message = "otp only have 4 character")
    private String otp;
    @NotBlank(message = "userId required")
    private String UserId;
}
