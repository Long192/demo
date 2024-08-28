package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Request.ForgotPasswordRequest;
import com.example.demo.Dto.Request.GetTokenRequest;
import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.RefreshTokenRequest;
import com.example.demo.Dto.Request.ResetPasswordRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.ForgotPasswordResponse;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "auth", description = "auth")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "signup",description = "sign up with email and password")
    @PostMapping(value = "/signup", consumes = {"application/json"})
    public ResponseEntity<CustomResponse<UserDto>> signup(
        @RequestBody @Valid SignUpRequest request
    ) throws Exception {
        return ResponseEntity.ok(CustomResponse.<UserDto>builder().data(authService.signUp(request)).build());
    }

    @Operation(summary = "login",description = "login with email and password to get login otp")
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<OtpDto>> loginOtp(@RequestBody @Valid LoginRequest request) throws Exception {
        return ResponseEntity.ok(CustomResponse.<OtpDto>builder().data(authService.loginOtp(request)).build());
    }

    @Operation(
        summary = "refresh",
        description = "send refresh token to get a new refresh token"
    )
    @PostMapping("/refresh")
    public ResponseEntity<CustomResponse<LoginResponse>> refresh(
        @RequestBody @Valid RefreshTokenRequest request
    ) throws Exception {
        return ResponseEntity.ok(
            CustomResponse.<LoginResponse>builder().data(authService.refreshToken(request.getRefreshToken())).build()
        );
    }

    @Operation(summary = "token", description = "get token with otp and user id")
    @PostMapping("/token")
    public ResponseEntity<CustomResponse<LoginResponse>> getToken(
        @RequestBody @Valid GetTokenRequest entity
    ) throws Exception {
        return ResponseEntity.ok(CustomResponse.<LoginResponse>builder().data(authService.login(entity)).build());
    }

    @Operation(summary = "forgot password", description = "get reset password url with email")
    @PostMapping("/forgot-password")
    public ResponseEntity<CustomResponse<ForgotPasswordResponse>> forgotPassword(
        @RequestBody @Valid ForgotPasswordRequest email
    ) throws Exception {
        return ResponseEntity.ok(
            CustomResponse.<ForgotPasswordResponse>builder().data(authService.forgotPassword(email.getEmail())).build()
        );
    }

    @Operation(summary = "reset password", description = "reset password with url get form forgot password request")
    @PostMapping("/reset-password")
    public ResponseEntity<CustomResponse<?>> resetPassword(
        @RequestBody @Valid ResetPasswordRequest request,
        @RequestParam String userId,
        @RequestParam String token
    ) throws Exception {
        authService.resetPassword(request.getPassword(), userId, token);
        return ResponseEntity.ok(new CustomResponse<>());
    }
}
