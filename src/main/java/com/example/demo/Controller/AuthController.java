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
import com.example.demo.Dto.Request.ResetPasswordRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Dto.Response.ApiResponse;
import com.example.demo.Dto.Response.ForgotPasswordResponse;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Service.AuthService;
import com.uploadcare.upload.UploadFailureException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping(value = "/signup", consumes = { "multipart/form-data" })
    public ApiResponse<MessageResponse> signupFormData(@ModelAttribute SignUpRequest request) throws MalformedURLException, UploadFailureException, ParseException {
        authService.signUp(request);
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
    
    @PostMapping(value = "/signup", consumes = { "application/json" })
    public ApiResponse<MessageResponse> signupJson(@RequestBody SignUpRequest request) throws MalformedURLException, UploadFailureException, ParseException {
        authService.signUp(request);
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @PostMapping("/login")
    public ApiResponse<OtpDto> loginOtp(@RequestBody LoginRequest request) {
        return ApiResponse.<OtpDto> builder().data(authService.loginOtp(request)).build();
    }

    @PostMapping("/token")
    public ApiResponse<LoginResponse> getToken(@RequestBody GetTokenRequest entity) {
        return ApiResponse.<LoginResponse> builder().data(authService.login(entity)).build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest email) {
        return ApiResponse.<ForgotPasswordResponse> builder().data(authService.forgotPassword(email.getEmail())).build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<MessageResponse> password(@RequestBody ResetPasswordRequest request, @RequestParam String userId, @RequestParam String token) {
        authService.resetPassword(request.getPassword(), userId, token);
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
}
