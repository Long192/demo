package com.example.demo.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.Request.FriendRequest;
import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Model.User;
import com.example.demo.Service.FriendService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class FriendControllerTest {

    User user1 = User.builder().id(1L).email("email1").fullname("fullname1").build();
    User user2 = User.builder().id(2L).email("email2").fullname("fullname2").build();
    User user3 = User.builder().id(3L).email("email3").fullname("fullname3").build();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FriendService friendService;
    @Autowired
    private ModelMapper mapper;

    private static String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    public void getFriendSuccess() throws Exception {
        Page<User> friends = new PageImpl<>(Arrays.asList(user1, user2, user3));

        when(friendService.getFriends(any(Pageable.class), anyString()))
                .thenReturn(mapper.map(friends, new TypeToken<CustomPage<UserDto>>() {}.getType()));

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

        mockMvc.perform(get("/friend").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page index must not be less than zero"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendFailedNegativePageSize() throws Exception {

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
    public void sendAddFriendRequestSuccess() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();
        UserDto userDto = UserDto.builder().id(2L).fullname("fullname").email("email@email.com").build();

        when(friendService.addFriend(2L)).thenReturn(userDto)
;
        mockMvc.perform(post("/friend").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").value(userDto));
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
        Page<User> friend = new PageImpl<>(Arrays.asList(user1, user2, user3));

        when(friendService.getFriendRequests(any(Pageable.class)))
                .thenReturn(mapper.map(friend, new TypeToken<CustomPage<UserDto>>() {}.getType()));

        mockMvc.perform(get("/friend/friend-requests"))
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

        mockMvc.perform(get("/friend/friend-requests"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("cannot find attribute"));
    }

    @Test
    @WithMockUser
    public void getFriendRequestFailedNegativePageIndex() throws Exception {
        mockMvc.perform(get("/friend/friend-requests")
                .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page index must not be less than zero"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void getFriendRequestFailedNegativePageSize() throws Exception {
        mockMvc.perform(get("/friend/friend-requests")
                .param("size", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("Page size must not be less than one"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void acceptFriendSuccess() throws Exception {
        
        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        UserDto userDto = mapper.map(user2, UserDto.class);

        when(friendService.acceptFriendRequest(req.getFriendId())).thenReturn(userDto);

        mockMvc.perform(post("/friend/accept-friend").content(asJsonString(req)).accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").value(userDto));
    }

    @Test
    @WithMockUser
    public void acceptFriendFailedFriendNotFound() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        doThrow(new Exception("user not found")).when(friendService).acceptFriendRequest(anyLong());

        mockMvc.perform(post("/friend/accept-friend").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message").value("user not found"));
    }

    @Test
    @WithMockUser
    public void acceptFriendFailedAlreadyFriend() throws Exception {

        FriendRequest req = FriendRequest.builder().friendId(2L).build();

        doThrow(new Exception("already friend")).when(friendService).acceptFriendRequest(anyLong());

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
                .andExpect(jsonPath("message").value("success"));
    }

    @Test
    @WithMockUser
    public void getFriendReceiverSuccess() throws Exception {
        UserDto userDto = mapper.map(user2, UserDto.class);
        CustomPage<UserDto> page = new CustomPage<>();
        page.setPageNumber(1);
        page.setTotalPages(1);
        page.setSize(1);
        page.setContent(List.of(userDto));
        page.setTotalElements(1);

        when(friendService.getFriendReceivers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/friend/friend-request-sent").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content[0].email").value(userDto.getEmail()));
    }

    @Test
    @WithMockUser
    public void rejectFriendSuccess() throws Exception {
        mockMvc.perform(delete("/friend/reject-friend/1").contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("status").value(200))
        .andExpect(jsonPath("message").value("success"));
    }

    @Test
    @WithMockUser
    public void cancelFriendSuccess() throws Exception {
        mockMvc.perform(delete("/friend/cancel-friend/1").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"));
    }

    
}
