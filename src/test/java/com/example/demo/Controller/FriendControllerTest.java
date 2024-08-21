package com.example.demo.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.Request.FriendRequest;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Model.User;
import com.example.demo.Service.FriendService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendService friendService;

    User user1 = User.builder().email("email1").fullname("fullname1").build();
    User user2 = User.builder().email("email2").fullname("fullname2").build();
    User user3 = User.builder().email("email3").fullname("fullname3").build();

    PostDto post1 = PostDto.builder().content("content 1").build();
    PostDto post2 = PostDto.builder().content("content 2").build();
    PostDto post3 = PostDto.builder().content("content 3").build();

    private static String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    public void getFriendSuccess() throws Exception {

        when(friendService.getFriends(any(Pageable.class), anyString()))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/friend")).andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content").exists())
                .andExpect(jsonPath("data.content[0].email").value("email1"))
                .andExpect(jsonPath("data.content[0].fullname").value("fullname1"))
                .andExpect(jsonPath("data.content[1].email").value("email2"))
                .andExpect(jsonPath("data.content[1].fullname").value("fullname2"))
                .andExpect(jsonPath("data.content[2].email").value("email3"))
                .andExpect(jsonPath("data.content[2].fullname").value("fullname3"));
    }

    @Test
    @WithMockUser
    public void getFriendFailedNegativePageIndex() throws Exception {

        when(friendService.getFriends(any(Pageable.class), anyString()))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/friend").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page index must not be less than zero"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendFailedNegativePageSize() throws Exception {

        when(friendService.getFriends(any(Pageable.class), anyString()))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/friend").param("size", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page size must not be less than one"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendFailedWrongSortBy() throws Exception {

        when(friendService.getFriends(any(Pageable.class), anyString()))
                .thenThrow(new Exception("cannot find attribute"));

        mockMvc.perform(get("/friend").param("sortBy", "asfasdfa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("cannot find attribute"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendPostSuccess() throws Exception {

        when(friendService.getFriendPost(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(post1, post2, post3)));

        mockMvc.perform(get("/friend/friend-posts")).andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content[0].content").value("content 1"))
                .andExpect(jsonPath("data.content[1].content").value("content 2"))
                .andExpect(jsonPath("data.content[2].content").value("content 3"));
    }

    @Test
    @WithMockUser
    public void getFriendPostFailedWrongSortBy() throws Exception {

        when(friendService.getFriendPost(any(Pageable.class))).thenThrow(new Exception("cannot find attribute"));

        mockMvc.perform(get("/friend/friend-posts")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("cannot find attribute"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendPostsFailedNegativePageIndex() throws Exception {

        when(friendService.getFriendPost(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(post1, post2, post3)));

        mockMvc.perform(get("/friend/friend-posts")
                .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page index must not be less than zero"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendPostsFailedNegativePageSize() throws Exception {

        when(friendService.getFriendPost(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(post1, post2, post3)));

        mockMvc.perform(get("/friend/friend-posts")
                .param("size", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page size must not be less than one"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void sendAddFriendRequestSuccess() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        mockMvc.perform(post("/friend").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.message").value("success"))
                .andExpect(jsonPath("data.status").value(true));
    }

    @Test
    @WithMockUser
    public void sendAddFriendRequestFailedFriendNotFound() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        doThrow(new Exception("user not found")).when(friendService).addFriend(anyLong());

        mockMvc.perform(post("/friend").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("message").value("user not found"));
    }

    @Test
    @WithMockUser
    public void sendAddFriendRequestFailedAlreadyFriendOrWaitingToBeAccept() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        doThrow(new Exception("already friend or waiting to be accept")).when(friendService).addFriend(anyLong());

        mockMvc.perform(post("/friend").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("already friend or waiting to be accept"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendRequestSuccess() throws Exception {
        
        when(friendService.getFriendRequests(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/friend/friend-request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content[0].email").value("email1"))
                .andExpect(jsonPath("data.content[0].fullname").value("fullname1"))
                .andExpect(jsonPath("data.content[1].email").value("email2"))
                .andExpect(jsonPath("data.content[1].fullname").value("fullname2"))
                .andExpect(jsonPath("data.content[2].email").value("email3"))
                .andExpect(jsonPath("data.content[2].fullname").value("fullname3"));
    }

    @Test
    @WithMockUser
    public void getFriendRequestFailedWrongSortBy() throws Exception {

        when(friendService.getFriendRequests(any(Pageable.class)))
                .thenThrow(new Exception("cannot find attribute"));

        mockMvc.perform(get("/friend/friend-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("cannot find attribute"));
    }

    @Test
    @WithMockUser
    public void getFriendRequestFailedNegativePageIndex() throws Exception {

        when(friendService.getFriendRequests(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/friend/friend-request")
                .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page index must not be less than zero"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendRequestFailedNegativePageSize() throws Exception {

        when(friendService.getFriendRequests(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(user1, user2, user3)));

        mockMvc.perform(get("/friend/friend-request")
                .param("size", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page size must not be less than one"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void acceptFriendSuccess() throws Exception {
        
        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        mockMvc.perform(post("/friend/accept-friend").content(asJsonString(req)).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.status").value(true))
                .andExpect(jsonPath("data.message").value("success"));
    }

    @Test
    @WithMockUser
    public void acceptFriendFailedFriendNotFound() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        doThrow(new Exception("user not found")).when(friendService).updateFriendStatus(anyLong());

        mockMvc.perform(post("/friend/accept-friend").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("user not found"));
    }

    @Test
    @WithMockUser
    public void acceptFriendFailedAlreadyFriend() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        doThrow(new Exception("already friend")).when(friendService).updateFriendStatus(anyLong());

        mockMvc.perform(post("/friend/accept-friend").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("already friend"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void deleteFriendSuccess() throws Exception {
        mockMvc.perform(delete("/friend/1"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.status").value(true))
                .andExpect(jsonPath("data.message").value("success"));
    }

    
}
