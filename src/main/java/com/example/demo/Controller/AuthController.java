package com.example.demo.Controller;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Service.AuthService;
import com.uploadcare.upload.UploadFailureException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "auth", description = "auth")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "signup", description = "sign up with email and password, use formdata if you want to upload avatar")
    @PostMapping(value = "/signup", consumes = { "multipart/form-data" })
    public CustomResponse<MessageResponse> signupFormData(@ModelAttribute @Valid SignUpRequest request)
            throws MalformedURLException, UploadFailureException, ParseException {
        authService.signUp(request);
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @Operation(summary = "signup", description = "sign up with email and password, use formdata if you want to upload avatar")
    @PostMapping(value = "/signup", consumes = { "application/json" })
    public CustomResponse<MessageResponse> signupJson(@RequestBody @Valid SignUpRequest request)
            throws MalformedURLException, UploadFailureException, ParseException {
        authService.signUp(request);
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @Operation(summary = "login", description = "login with email and password to get login otp")
    @PostMapping("/login")
    public CustomResponse<OtpDto> loginOtp(@RequestBody LoginRequest request) throws Exception {
        return CustomResponse.<OtpDto> builder().data(authService.loginOtp(request)).build();
    }

    @Operation(summary = "refresh", description = "send refresh token to get a new refresh token")
    @PostMapping("/refresh")
    public CustomResponse<LoginResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) throws Exception {
        return CustomResponse.<LoginResponse>builder().data(authService.refreshToken(request.getRefreshToken())).build();
    }

    @Operation(summary = "token", description = "get token with otp and user id")
    @PostMapping("/token")
    public CustomResponse<LoginResponse> getToken(@RequestBody GetTokenRequest entity) {
        return CustomResponse.<LoginResponse> builder().data(authService.login(entity)).build();
    }

    @Operation(summary = "forgot password", description = "get reset password url with email")
    @PostMapping("/forgot-password")
    public CustomResponse<ForgotPasswordResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest email)
            throws Exception {
        return CustomResponse.<ForgotPasswordResponse> builder().data(authService.forgotPassword(email.getEmail()))
                .build();
    }

    @Operation(summary = "reset password", description = "reset password with url get form forgot password request")
    @PostMapping("/reset-password")
    public CustomResponse<MessageResponse> password(@RequestBody ResetPasswordRequest request,
            @RequestParam String userId, @RequestParam String token) throws Exception {
        authService.resetPassword(request.getPassword(), userId, token);
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
}
