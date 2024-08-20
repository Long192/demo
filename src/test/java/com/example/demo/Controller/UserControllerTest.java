package com.example.demo.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.Model.User;
import com.example.demo.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    User user1 = User.builder().email("email1").fullname("fullname1").build();
    User user2 = User.builder().email("email2").fullname("fullname2").build();
    User user3 = User.builder().email("email3").fullname("fullname3").build();

    private static String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    public void getAllUserSuccess() throws Exception {

        when(userService.findAll(any(Pageable.class), anyString()))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content[0].email").value(user1.getEmail()))
                .andExpect(jsonPath("data.content[0].fullname").value(user1.getFullname()))
                .andExpect(jsonPath("data.content[1].email").value(user2.getEmail()))
                .andExpect(jsonPath("data.content[1].fullname").value(user2.getFullname()))
                .andExpect(jsonPath("data.content[2].email").value(user3.getEmail()))
                .andExpect(jsonPath("data.content[2].fullname").value(user3.getFullname()));
    }
}
