package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetTokenRequest {
    @Size(min = 4, max = 4, message = "otp only have 4 character")
    @NotNull(message = "otp required")
    private String otp;
    @NotNull(message = "userId required")
    private Long UserId;
}
