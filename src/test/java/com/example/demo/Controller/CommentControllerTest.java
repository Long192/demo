package com.example.demo.Controller;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.Dto.Request.AddCommentRequest;
import com.example.demo.Model.User;
import com.example.demo.Service.CommentService;
import com.example.demo.Service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private CommentController commentController;

    @MockBean
    private CommentService commentService;

    @MockBean
    private PostService postService;

//    @BeforeEach
//    public void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
//        UserDetails mockUser = User.builder().id(1L).email("test@test.com").password("password").build();
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn(mockUser);
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//    }

    private static String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    public void addCommentSuccess() throws Exception {
        AddCommentRequest req = AddCommentRequest.builder().content("comment 1").postId(1L).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/comment").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.status").value(true));
    }

    @Test
    @WithMockUser
    public void addCommentFailedEmpty() throws Exception {
        AddCommentRequest req = AddCommentRequest.builder().content("").postId(1L).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/comment")
                .content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("content required"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

}
