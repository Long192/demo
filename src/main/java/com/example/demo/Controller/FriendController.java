package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.CustomResponse;
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
    private FriendService friendService;

    @Operation(
        summary = "get friend",
        description = "get a list of friends based on the token of the currently logged in user"
    )
    @GetMapping("")
    public ResponseEntity<CustomResponse<CustomPage<UserDto>>> getFriend(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(CustomResponse.<CustomPage<UserDto>>builder()
                .data(friendService.getFriends(pageable, search))
                .build());
    }

    @Operation(summary = "add new friend", description = "add a new friend")
    @PostMapping("")
    public ResponseEntity<CustomResponse<UserDto>> addFriend(@RequestBody @Valid FriendRequest request) throws Exception {
        return ResponseEntity.ok(CustomResponse.<UserDto>builder()
                .data(friendService.addFriend(request.getFriendId())).build());
    }

    @Operation(summary = "list friend receiver", description = "list all friend request you received from other user")
    @GetMapping("/friend-requests")
    public ResponseEntity<CustomResponse<CustomPage<UserDto>>> getFriendRequest(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(CustomResponse.<CustomPage<UserDto>>builder()
                .data(friendService.getFriendRequests(pageRequest))
                .build());
    }

    @Operation(summary = "list friend request", description = "list all friend request you sent to other user")
    @GetMapping("/friend-request-sent")
    public ResponseEntity<CustomResponse<CustomPage<UserDto>>> getFriendRecivers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(CustomResponse.<CustomPage<UserDto>>builder()
                .data(friendService.getFriendReceivers(pageRequest))
                .build());
    }

    @Operation(summary = "accept friend request", description = "accept a friend request")
    @PostMapping("/accept-friend")
    public ResponseEntity<CustomResponse<UserDto>> acceptFriendRequest(@RequestBody @Valid FriendRequest request)
        throws Exception {
        return ResponseEntity.ok(CustomResponse.<UserDto>builder()
                .data(friendService.acceptFriendRequest(request.getFriendId())).build());
    }

    @Operation(summary = "reject friend request", description = "reject a friend request")
    @DeleteMapping("/reject-friend/{id}")
    public ResponseEntity<CustomResponse<UserDto>> rejectFriendRequest(@PathVariable Long id)
        throws Exception {
        friendService.rejectFriendRequest(id);
        return ResponseEntity.ok(new CustomResponse<>());
    }

    @Operation(summary = "delete friend", description = "delete a friend")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<?>>deleteFriend(@PathVariable Long id) throws Exception {
        friendService.removeFriend(id);
        return ResponseEntity.ok(new CustomResponse<>());
    }

    @Operation(summary = "cancel friend request", description = "cancel a friend request")
    @DeleteMapping("/cancel-friend/{id}")
    public ResponseEntity<CustomResponse<?>> cancelFriendRequest(@PathVariable Long id) throws Exception {
        friendService.cancelFriendRequest(id);
        return ResponseEntity.ok(new CustomResponse<>());
    }
}
