package com.example.demo.Controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    public void signUpTest() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("javatest@javatest.com").password("testpassword").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/signup").content(asJsoString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists());
    }

    @Test
    public void LoginTest() throws Exception {
        LoginRequest request = LoginRequest.builder().email("email@email.com").password("password").build();
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login").content(asJsoString(request))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists());
    }

    private static String asJsoString(final Object obj) throws Exception {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw e;
        }
    }
}
