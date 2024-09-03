package com.example.demo.Controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import com.example.demo.Dto.Response.UserDto;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.Request.UpdateUserRequest;
import com.example.demo.Model.User;
import com.example.demo.Service.ExcelService;
import com.example.demo.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private ExcelService excelService;

    User user1 = User.builder().email("email1").fullname("fullname1").build();
    User user2 = User.builder().email("email2").fullname("fullname2").build();
    User user3 = User.builder().email("email3").fullname("fullname3").build();
    @Autowired
    private ModelMapper mapper;

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

    @Test
    @WithMockUser
    public void getAllUserFailedNegativePageIndex () throws Exception {

        when(userService.findAll(any(Pageable.class), anyString()))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/user")
                .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page index must not be less than zero"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getAllUserFailedNegativePageSize () throws Exception {

        when(userService.findAll(any(Pageable.class), anyString()))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/user")
                .param("size", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page size must not be less than one"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getAllUserFailedWrongSortBy () throws Exception {

        when(userService.findAll(any(Pageable.class), anyString()))
                .thenThrow(new Exception("wrong sort order"));

        mockMvc.perform(get("/user"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("wrong sort order"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void reportUserSuccess() throws Exception {
        mockMvc.perform(get("/user/report"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=users_")))
                .andExpect(content().contentType("application/octet-stream"));
    }

    @Test
    @WithMockUser
    public void updateUserSuccessJsonData() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .fullname("fullname")
                .dob("2002-09-01")
                .address("new address")
                .etc("new etc")
                .build();

        UserDto userDto = UserDto.builder().email("email@email.com").fullname("fullname").build();

        when(userService.updateUser(req)).thenReturn(userDto);

        mockMvc.perform(put("/user")
                .content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").value(userDto));

    }
}
