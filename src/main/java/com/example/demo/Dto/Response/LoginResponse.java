package com.example.demo.Dto.Response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private Long id;
    private String token;
    private String refreshToken;
    private String email;
    private String fullname;
    private String address;
    private String avatar;
    private String dob;
}
