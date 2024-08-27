package com.example.demo.Controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
import com.example.demo.Dto.Response.IdResponse;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Enum.OrderEnum;
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

    @Operation(
        summary = "get friend",
        description = "get a list of friends based on the token of the currently logged in user"
    )
    @GetMapping("")
    public ResponseEntity<CustomResponse<Page<UserDto>>> getFriend(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<UserDto> friendList =
            friendService.getFriends(pageable, search).map(source -> mapper.map(source, UserDto.class));
        return ResponseEntity.ok(CustomResponse.<Page<UserDto>>builder().data(friendList).build());
    }

    @Operation(summary = "get friend post", description = "get a list of friends' posts from 1 week ago to the present")
    @GetMapping("/friend-posts")
    public ResponseEntity<CustomResponse<Page<PostDto>>> getFriendPost(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy
    ) throws Exception {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            CustomResponse.<Page<PostDto>>builder().data(friendService.getFriendPost(pageable)).build()
        );
    }

    @Operation(summary = "add new friend", description = "add a new friend")
    @PostMapping("")
    public ResponseEntity<CustomResponse<IdResponse>> addFriend(@RequestBody @Valid FriendRequest request) throws Exception {
        friendService.addFriend(request.getFriendId());
        return ResponseEntity.ok(CustomResponse.<IdResponse>builder().data(new IdResponse()).build());
    }

    @Operation(summary = "list friend request", description = "list all friend request you received from other user")
    @GetMapping("/friend-request")
    public ResponseEntity<CustomResponse<Page<UserDto>>> getFriendRequest(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<UserDto> friendList =
                friendService.getFriendRequests(pageRequest).map(source -> mapper.map(source, UserDto.class));
        return ResponseEntity.ok(CustomResponse.<Page<UserDto>>builder().data(friendList).build());
    }

    @Operation(summary = "accept friend request", description = "accept a friend request")
    @PostMapping("/accept-friend")
    public ResponseEntity<CustomResponse<IdResponse>> acceptFriendRequest(@RequestBody @Valid FriendRequest request)
        throws Exception {
        friendService.updateFriendStatus(request.getFriendId());
        return ResponseEntity.ok(CustomResponse.<IdResponse>builder().data(new IdResponse()).build());
    }

    @Operation(summary = "delete friend", description = "delete a friend")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<IdResponse> >deleteFriend(@PathVariable Long id) throws Exception {
        friendService.removeFriend(id);
        return ResponseEntity.ok(CustomResponse.<IdResponse>builder().data(new IdResponse()).build());
    }
}
