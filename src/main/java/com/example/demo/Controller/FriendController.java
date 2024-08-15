package com.example.demo.Controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Request.FriendRequest;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Model.User;
import com.example.demo.Service.FriendService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "friend", description = "friendship")
@RestController
@RequestMapping("/friend")
public class FriendController {
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private FriendService friendService;

    @Operation(summary = "get friend", description = "get a list of friends based on the token of the currently logged in user")
    @GetMapping("")
    public CustomResponse<List<UserDto>> getFriend(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        List<UserDto> friendList = mapper.map(friendService.getFriends(pageable, search), new TypeToken<List<UserDto>>() {}.getType());
        return CustomResponse.<List<UserDto>> builder().data(friendList).build();
    }

    @Operation(summary = "get friend post", description = "get a list of friends' posts from 1 week ago to the present")
    @GetMapping("/friend-posts")
    public CustomResponse<Page<PostDto>> getFriendPost(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        return CustomResponse.<Page<PostDto>> builder().data(friendService.getFriendPost(pageable)).build();
    }

    @Operation(summary = "add new friend", description = "add a new friend")
    @PostMapping("")
    public CustomResponse<MessageResponse> addFriend(@RequestBody @Valid FriendRequest request) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        friendService.addFriend(user.getId(), request.getFriendId());
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @Operation(summary = "list friend request", description = "list all friend request you received from other user")
    @GetMapping("/friend-request")
    public CustomResponse<List<UserDto>> getFriendRequest(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<UserDto> friendList =
                mapper.map(friendService.getFriendRequests(pageRequest), new TypeToken<List<UserDto>>() {}.getType());
        return CustomResponse.<List<UserDto>> builder().data(friendList).build();
    }

    @Operation(summary = "accept friend request", description = "accept a friend request")
    @PostMapping("/accept-friend")
    public CustomResponse<MessageResponse> acceptFriendRequest(@RequestBody @Valid FriendRequest request)
            throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        friendService.updateFriendStatus(request.getFriendId(), user.getId());
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @Operation(summary = "delete friend", description = "delete a friend")
    @DeleteMapping("/{id}")
    public CustomResponse<MessageResponse> deleteFriend(@PathVariable String id) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        friendService.removeFriend(user.getId(), Long.valueOf(id));
        return CustomResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
}
