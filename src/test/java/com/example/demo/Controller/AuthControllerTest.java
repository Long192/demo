package com.example.demo.Controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.Request.ForgotPasswordRequest;
import com.example.demo.Dto.Request.GetTokenRequest;
import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.RefreshTokenRequest;
import com.example.demo.Dto.Request.ResetPasswordRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Dto.Response.ForgotPasswordResponse;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Exception.CustomException;
import com.example.demo.Service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private static String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    public void signUpTestOnlyUserAndPasswordSuccess() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("javatest@javatest.com").password("testpassword").build();

        mockMvc.perform(post("/auth/signup").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value("200"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.message").value("success"))
                .andExpect(jsonPath("data.status").value(true));
    }

    @Test
    public void SignupWithWrongEmailFormat() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("java test").password("password").build();

        mockMvc.perform(post("/auth/signup").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect((jsonPath("message").value("wrong email format")));
    }

    @Test
    public void SignupWithEmailEmpty() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("").password("password").build();

        mockMvc.perform(post("/auth/signup").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect((jsonPath("message").value("email required")));
    }

    @Test
    public void SignupWithEmailNull() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email(null).password("password").build();

        mockMvc.perform(post("/auth/signup").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect((jsonPath("message").value("email required")));
    }

    @Test
    public void SignupWithPasswordEmpty() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password("").build();

        mockMvc.perform(post("/auth/signup").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect((jsonPath("message").value("password need at least 6 character")));
    }

    @Test
    public void SignupWithPasswordNull() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password(null).build();

        mockMvc.perform(post("/auth/signup").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect((jsonPath("message").value("password required")));
    }

    @Test
    public void SignupWithAvatar() throws Exception {
        MockMultipartFile mockImg = new MockMultipartFile(
                "avatar",
                "img.jpeg",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "image".getBytes()
        );

        SignUpRequest req = SignUpRequest.builder()
            .email("email@email.com")
            .password("password")
            .avatar(mockImg)
            .build();

        mockMvc.perform(multipart(HttpMethod.POST,"/auth/signup")
            .file(mockImg)
            .param("email", req.getEmail())
            .param("password", req.getPassword())
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("message").value("success"))
            .andExpect(jsonPath("status").value(200))
            .andExpect(jsonPath("data").exists());
    }

    @Test
    public void SignupWithPasswordLessthanMinimum() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password("sdfdf").build();

        mockMvc.perform(post("/auth/signup").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect((jsonPath("message").value("password need at least 6 character")));
    }

    @Test
    public void LoginTestSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder().email("email@email.com").password("password").build();

        OtpDto response = OtpDto.builder().UserId(1L).otp("3211").build();

        when(authService.loginOtp(request)).thenReturn(response);

        mockMvc.perform(post("/auth/login").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value("200"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.otp").value(response.getOtp()))
                .andExpect(jsonPath("data.userId").value(response.getUserId()));
    }

    @Test
    public void LoginTestFailedWrongEmailOrPassword() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email@test.com").password("test").build();

        when(authService.loginOtp(req)).thenThrow(new CustomException(404, "user not found"));

        mockMvc.perform(post("/auth/login").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("user not found"))
                .andExpect(jsonPath("status").value(404));
    }

    @Test
    public void LoginTestFailedWrongEmailFormat() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email").password("test").build();

        mockMvc.perform(post("/auth/login").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("wrong email format"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void LoginTestFailedEmailEmpty() throws Exception {
        LoginRequest req = LoginRequest.builder().email("").password("test").build();

        mockMvc.perform(post("/auth/login").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("email required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void LoginTestFailedPasswordEmpty() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("").build();

        mockMvc.perform(post("/auth/login").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("password required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenSuccess() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("3421").build();

        mockMvc.perform(post("/auth/token").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200));
    }

    @Test
    public void GetJwtTokenFailedWrongOpt() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("3421").build();

        when(authService.login(req)).thenThrow(new BadCredentialsException("otp or user invalid"));

        mockMvc.perform(post("/auth/token").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("otp or user invalid"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenFailedOtpSize() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("34213").build();

        mockMvc.perform(post("/auth/token").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("otp only have 4 character"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenFailedOtpNull() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp(null).build();

        mockMvc.perform(post("/auth/token").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("otp required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenFailedUserIdNull() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(null).otp("3423").build();

        mockMvc.perform(post("/auth/token").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("userId required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void refreshTokenSuccess() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("token").build();

        LoginResponse res = LoginResponse.builder().token("new token").build();

        when(authService.refreshToken(req.getRefreshToken())).thenReturn(res);

        mockMvc.perform(post("/auth/refresh").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.token").value(res.getToken()));
    }

    @Test
    public void refreshTokenFailureInvalidToken() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("token").build();

        when(authService.refreshToken(req.getRefreshToken())).thenThrow(new JwtException("jwt token error"));

        mockMvc.perform(post("/auth/refresh").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("jwt token error"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void refreshTokenFailureTokenNull() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken(null).build();

        mockMvc.perform(post("/auth/refresh").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("refreshToken required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void refreshTokenFailureTokenBlank() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("").build();

        mockMvc.perform(post("/auth/refresh").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("refreshToken required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void forgotPasswordSuccess() throws Exception {
        ForgotPasswordRequest req = ForgotPasswordRequest.builder().email("email@email.com").build();

        ForgotPasswordResponse res = ForgotPasswordResponse.builder().url("url").build();

        when(authService.forgotPassword(req.getEmail())).thenReturn(res);

        mockMvc.perform(post("/auth/forgot-password").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.url").value(res.getUrl()));
    }

    @Test
    public void forgotPasswordFailedEmailWrongFormat() throws Exception {
        ForgotPasswordRequest req = ForgotPasswordRequest.builder().email("email").build();

        mockMvc.perform(post("/auth/forgot-password").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("wrong email format"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void forgotPasswordFailedUserNotfound() throws Exception {
        ForgotPasswordRequest req = ForgotPasswordRequest.builder().email("email@test.com").build();

        when(authService.forgotPassword(req.getEmail())).thenThrow(new Exception("user not found"));

        mockMvc.perform(post("/auth/forgot-password").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("user not found"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void resetPasswordSuccess() throws Exception {
        ResetPasswordRequest req = ResetPasswordRequest.builder().password("password").build();

        mockMvc.perform(post("/auth/reset-password").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .queryParam("userId", "1").queryParam("token", "token"))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200));
    }

    @Test
    public void resetPasswordFailedUserNotFound() throws Exception {
        ResetPasswordRequest req = ResetPasswordRequest.builder().password("password").build();

        doThrow(new Exception("user not found"))
                .when(authService).resetPassword(req.getPassword(), "1", "token");

        mockMvc.perform(post("/auth/reset-password").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .queryParam("userId", "1").queryParam("token", "token"))
                .andExpect(jsonPath("message").value("user not found"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    public void resetPasswordFailedUserIdInvalid() throws Exception {
        ResetPasswordRequest req = ResetPasswordRequest.builder().password("password").build();

        doThrow(new NumberFormatException()).when(authService).resetPassword(req.getPassword(), "1", "token");

        mockMvc.perform(post("/auth/reset-password").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .queryParam("userId", "1").queryParam("token", "token"))
                .andExpect(jsonPath("message").value("number format error"))
                .andExpect(jsonPath("status").value(400));
    }
}
