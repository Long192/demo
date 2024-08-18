package com.example.demo.Controller;

import com.example.demo.Dto.Request.*;
import com.example.demo.Dto.Response.ForgotPasswordResponse;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private static String asJsoString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    public void signUpTestOnlyUserAndPasswordSuccess() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("javatest@javatest.com").password("testpassword").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("data.message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("data.status").value(true));
    }

    @Test
    public void SignupWithWrongEmailFormat() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("java test").password("password").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
            .andExpect((MockMvcResultMatchers.jsonPath("message").value("wrong email format")));
    }

    @Test
    public void SignupWithEmailEmpty() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("").password("password").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
            .andExpect((MockMvcResultMatchers.jsonPath("message").value("email required")));
    }

    @Test
    public void SignupWithEmailNull() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email(null).password("password").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
            .andExpect((MockMvcResultMatchers.jsonPath("message").value("email required")));
    }

    @Test
    public void SignupWithPasswordEmpty() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password("").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
            .andExpect((MockMvcResultMatchers.jsonPath("message").value("password need at least 6 character")));
    }

    @Test
    public void SignupWithPasswordNull() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password(null).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
            .andExpect((MockMvcResultMatchers.jsonPath("message").value("password required")));
    }

    // @Test
    // public void SignupWithAvatar() throws Exception {
    //     MockMultipartFile mockImg = new MockMultipartFile(
    //             "data",
    //             "img.jpeg",
    //             MediaType.IMAGE_JPEG_VALUE,
    //             "image".getBytes()
    //     );

    //     SignUpRequest req = SignUpRequest.builder()
    //         .email("email@email.com")
    //         .password("password")
    //         .build();

    //     mockMvc.perform(MockMvcRequestBuilders.multipart("/auth/signup").file(mockImg)
    //         .param("email", req.getEmail())
    //         .param("password", req.getPassword())
    //         .flashAttr("request", req)
    //         .contentType(MediaType.MULTIPART_FORM_DATA)
    //         .accept(MediaType.MULTIPART_FORM_DATA))
    //         .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
    //         .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
    //         .andExpect(MockMvcResultMatchers.jsonPath("data").exists());
    // }

    @Test
    public void SignupWithPasswordLessthanMinimum() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password("sdfdf").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
            .andExpect((MockMvcResultMatchers.jsonPath("message").value("password need at least 6 character")));
    }

    @Test
    public void LoginTestSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder().email("email@email.com").password("password").build();

        OtpDto response = OtpDto.builder().UserId(1L).otp("3211").build();

        when(authService.loginOtp(request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login").content(asJsoString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value("200"))
            .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("data.otp").value(response.getOtp()))
            .andExpect(MockMvcResultMatchers.jsonPath("data.userId").value(response.getUserId()));
    }

    @Test
    public void LoginTestFailedWrongEmailOrPassword() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email@test.com").password("test").build();

        when(authService.loginOtp(req)).thenThrow(new Exception("user not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("user not found"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void LoginTestFailedWrongEmailFormat() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email").password("test").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("wrong email format"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void LoginTestFailedEmailEmpty() throws Exception {
        LoginRequest req = LoginRequest.builder().email("").password("test").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("email required"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void LoginTestFailedPasswordEmpty() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("password required"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenSuccess() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("3421").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/token").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(200));
    }

    @Test
    public void GetJwtTokenFailedWrongOpt() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("3421").build();

        when(authService.login(req)).thenThrow(new BadCredentialsException("otp or user invalid"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/token").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("otp or user invalid"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenFailedOtpSize() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("34213").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/token").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("otp only have 4 character"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenFailedOtpNull() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp(null).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/token").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("otp required"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void GetJwtTokenFailedUserIdNull() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(null).otp("3423").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/token").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("userId required"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void refreshTokenSuccess() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("token").build();

        LoginResponse res = LoginResponse.builder().token("new token").build();

        when(authService.refreshToken(req.getRefreshToken())).thenReturn(res);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("data.token").value(res.getToken()));
    }

    @Test
    public void refreshTokenFailureInvalidToken() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("token").build();

        when(authService.refreshToken(req.getRefreshToken())).thenThrow(new JwtException("jwt token error"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("jwt token error"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void refreshTokenFailureTokenNull() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken(null).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("refreshToken required"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void refreshTokenFailureTokenBlank() throws Exception {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/refresh").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("refreshToken required"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void forgotPasswordSuccess() throws Exception {
        ForgotPasswordRequest req = ForgotPasswordRequest.builder().email("email@email.com").build();

        ForgotPasswordResponse res = ForgotPasswordResponse.builder().url("url").build();

        when(authService.forgotPassword(req.getEmail())).thenReturn(res);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/forgot-password").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("data.url").value(res.getUrl()));
    }

    @Test
    public void forgotPasswordFailedEmailWrongFormat() throws Exception {
        ForgotPasswordRequest req = ForgotPasswordRequest.builder().email("email").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/forgot-password").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("wrong email format"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void forgotPasswordFailedUserNotfound() throws Exception {
        ForgotPasswordRequest req = ForgotPasswordRequest.builder().email("email@test.com").build();

        when(authService.forgotPassword(req.getEmail())).thenThrow(new Exception("user not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/forgot-password").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("user not found"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void resetPasswordSuccess() throws Exception {
        ResetPasswordRequest req = ResetPasswordRequest.builder().password("password").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .queryParam("userId", "1").queryParam("token", "token"))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(200));
    }

    @Test
    public void resetPasswordFailedUserNotFound() throws Exception {
        ResetPasswordRequest req = ResetPasswordRequest.builder().password("password").build();

        doThrow(new Exception("user not found"))
            .when(authService).resetPassword(req.getPassword(), "1", "token");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .queryParam("userId", "1").queryParam("token", "token"))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("user not found"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    public void resetPasswordFailedUserIdInvalid() throws Exception {
        ResetPasswordRequest req = ResetPasswordRequest.builder().password("password").build();

        doThrow(new NumberFormatException()).when(authService).resetPassword(req.getPassword(), "1", "token");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/reset-password").content(asJsoString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .queryParam("userId", "1").queryParam("token", "token"))
            .andExpect(MockMvcResultMatchers.jsonPath("message").value("number format error"))
            .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }
}
