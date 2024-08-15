package com.example.demo.Controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.Dto.Response.OtpDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    public void signUpTestOnlyUserAndPasswordSuccess() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("javatest@javatest.com").password("testpassword").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
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
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect((MockMvcResultMatchers.jsonPath("message").value("wrong email format")));
    }

    @Test
    public void SignupWithEmailEmpty() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("").password("password").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect((MockMvcResultMatchers.jsonPath("message").value("email required")));
    }

    @Test
    public void SignupWithPasswordEmpty() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password("").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect((MockMvcResultMatchers.jsonPath("message").value("password required")));
    }

    @Test
    public void SignupWithPasswordLessthanMinimum() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@email.com").password("sdfdf").build();

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect((MockMvcResultMatchers.jsonPath("message").value("password need at least 6 character")));
    }

    @Test
    public void SignupWithAvatar() throws Exception {
        MockMultipartFile mockImg = new MockMultipartFile(
                "data",
                "img.jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                "image".getBytes()
        );

        HashMap<String, String> req = new HashMap<>();
        req.put("email", "email@email.com");
        req.put("password", "password");

        MediaType mediaType = new MediaType("multipart", "form-data", req);

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup")

                .contentType(mediaType).accept(MediaType.MULTIPART_FORM_DATA))
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
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value("200"))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.otp").value(response.getOtp()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.userId").value(response.getUserId()));
    }

    private static String asJsoString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
