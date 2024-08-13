package com.example.demo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Request.FriendRequest;
import com.example.demo.Dto.Response.ApiResponse;
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Model.User;
import com.example.demo.Service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/add-friend")
    public ApiResponse<MessageResponse> addFriend(@RequestBody @Valid FriendRequest request) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.addFriend(user.getId(), request.getFriendId());
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @GetMapping("/friend/{id}")
    public ApiResponse<List<UserDto>> getFriend(@PathVariable String id) throws NumberFormatException, Exception {
        return ApiResponse.<List<UserDto>> builder().data(userService.getFriends(Long.valueOf(id))).build();
    }

    @GetMapping("/friend-request/{id}")
    public ApiResponse<List<UserDto>> getFriendRequest(@PathVariable String id)
            throws NumberFormatException, Exception {
        return ApiResponse.<List<UserDto>> builder().data(userService.getFriendRequests(Long.valueOf(id))).build();
    }

    @PostMapping("/accept-friend")
    public ApiResponse<MessageResponse> acceptFriendRequest(@RequestBody @Valid FriendRequest request) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updateFriendStatus(request.getFriendId(), user.getId());
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @PostMapping("/delete-friend")
    public ApiResponse<MessageResponse> deleteFriend(@RequestBody @Valid FriendRequest request) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.removeFriend(user.getId(), request.getFriendId());
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
}
