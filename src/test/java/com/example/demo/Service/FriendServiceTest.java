package com.example.demo.Service;

import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Friend;
import com.example.demo.Model.User;
import com.example.demo.Repository.FriendRepository;
import com.example.demo.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class FriendServiceTest {
    User user = User.builder().id(1L).email("email@email.com").fullname("fullname").build();
    User user1 = User.builder().id(2L).email("email@email.com").fullname("fullname").build();
    User user2 = User.builder().id(3L).email("email@email.com").fullname("fullname").build();
    User user3 = User.builder().id(4L).email("email@email.com").fullname("fullname").build();
    Friend friend1 = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.accepted)
            .build();
    Friend friend2 = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.accepted)
            .build();
    Friend friend3 = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.accepted)
            .build();
    @InjectMocks
    private FriendService friendService;
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private PostService postService;
    @Mock
    private UserService userService;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void getFriendPostSuccess() throws Exception {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageable = PageRequest.of(0, 10, sort);
        PostDto postDto1 = PostDto.builder().content("content").createdAt(timestamp)
                .user(mapper.map(user1, UserDto.class)).build();
        PostDto postDto2 = PostDto.builder().content("content").createdAt(timestamp)
                .user(mapper.map(user2, UserDto.class)).build();
        PostDto postDto3 = PostDto.builder().content("content").createdAt(timestamp)
                .user(mapper.map(user3, UserDto.class)).build();

        when(friendRepository.findAllFriends(user.getId())).thenReturn(List.of(friend1, friend2, friend3));
        when(postService.findPostByUserIdsAndCreatedAt(any(), any(Timestamp.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(postDto1, postDto2, postDto3), pageable, 3));

        Page<PostDto> res = friendService.getFriendPost(pageable);

        assertNotNull(res);
        assertEquals(res.getContent(), List.of(postDto1, postDto2, postDto3));
    }

    @Test
    public void getFriendPostFailWrongSortBy() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "wrong property");
        PageRequest pageable = PageRequest.of(0, 10, sort);

        when(friendRepository.findAllFriends(user.getId())).thenReturn(List.of(friend1, friend2, friend3));
        when(postService.findPostByUserIdsAndCreatedAt(any(), any(Timestamp.class), any(Pageable.class)))
                .thenThrow(new InvalidDataAccessApiUsageException("wrong sort by"));

        Exception exception =
                assertThrows(Exception.class, () -> friendService.getFriendPost(pageable));

        assertNotNull(exception);
        assertEquals("wrong sort by", exception.getMessage());
    }

    @Test
    public void addFriendSuccess() throws Exception {
        Friend friend = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.pending)
                .build();

        when(userService.findById(user.getId())).thenReturn(user);
        when(userService.findById(user1.getId())).thenReturn(user1);
        when(friendRepository.findByFriendRequesterAndFriendReceiver(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        friendService.addFriend(user1.getId());

        ArgumentCaptor<Friend> friendCaptor = ArgumentCaptor.forClass(Friend.class);

        verify(friendRepository, times(1)).save(friendCaptor.capture());

        Friend friendCaptured = friendCaptor.getValue();

        assertNotNull(friendCaptured);
        assertEquals(friendCaptured, friend);
    }

    @Test
    public void addFriendFailedUserNotFound() throws Exception {
        when(userService.findById(user.getId())).thenThrow(new CustomException(404, "user not found"));

        CustomException exception = assertThrows(CustomException.class, () -> friendService.addFriend(user1.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "user not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void addFriendFailedRequestExist() throws Exception {
        Friend friend = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.pending)
                .build();

        when(userService.findById(user.getId())).thenReturn(user);
        when(userService.findById(user1.getId())).thenReturn(user1);
        when(friendRepository.findByFriendRequesterAndFriendReceiver(anyLong(), anyLong()))
                .thenReturn(Optional.of(friend));

        Exception exception = assertThrows(Exception.class, () -> friendService.addFriend(user1.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "already friend or waiting to be accept");
    }

    @Test
    public void updateFriendStatusSuccess() throws Exception {
        Friend pendingFriend =
                Friend.builder().friendReceiver(user).friendRequester(user1).status(FriendStatusEnum.pending).build();

        when(friendRepository.findByFriendRequesterAndFriendReceiver(anyLong(), anyLong()))
                .thenReturn(Optional.of(pendingFriend));

        friendService.updateFriendStatus(user.getId());

        ArgumentCaptor<Friend> friendCaptor = ArgumentCaptor.forClass(Friend.class);
        verify(friendRepository, times(1)).save(friendCaptor.capture());

        Friend friendCaptured = friendCaptor.getValue();

        assertNotNull(friendCaptured);
        assertEquals(friendCaptured.getStatus(), FriendStatusEnum.accepted);
        assertEquals(friendCaptured.getFriendRequester(), user1);
        assertEquals(friendCaptured.getFriendReceiver(), user);
    }

    @Test
    public void updateFriendStatusFailedUserNotFound() throws Exception {
        when(friendRepository.findByFriendRequesterAndFriendReceiver(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> friendService.updateFriendStatus(user1.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "friend not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void updateFriendFailedAlreadyFriend() throws Exception {
        when(friendRepository.findByFriendRequesterAndFriendReceiver(anyLong(), anyLong()))
                .thenReturn(Optional.of(friend1));

        CustomException exception = assertThrows(CustomException.class,
                () -> friendService.updateFriendStatus(user1.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "already friend");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void removeFriendSuccess() throws Exception {
        when(friendRepository.findByFriendRequesterAndFriendReceiver(anyLong(), anyLong()))
                .thenReturn(Optional.of(friend1));

        ArgumentCaptor<Friend> friendCaptor = ArgumentCaptor.forClass(Friend.class);

        friendService.removeFriend(user1.getId());
        verify(friendRepository, times(1)).delete(friendCaptor.capture());

        Friend friendCaptured = friendCaptor.getValue();
        assertNotNull(friendCaptured);
        assertEquals(friendCaptured, friend1);
    }

    @Test
    public void removeFriendFailedUserNotFound() throws Exception {
        when(friendRepository.findByFriendRequesterAndFriendReceiver(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> friendService.removeFriend(user1.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "friend not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void getFriendSuccess() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageable = PageRequest.of(0, 10, sort);
        when(friendRepository.findFriends(anyLong(), anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(friend1, friend2, friend3)));

        Page<User> users = friendService.getFriends(pageable, "");

        assertNotNull(users);
        assertNotNull(users.getContent());
        assertEquals(users.getSize(), 3);
    }

    @Test
    public void getFriendFailedWrongSortBy() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "wrong property");
        PageRequest pageable = PageRequest.of(0, 10, sort);
        when(friendRepository.findFriends(anyLong(), anyString(), any(Pageable.class)))
                .thenThrow(new InvalidDataAccessApiUsageException("wrong sort by"));

        Exception exception = assertThrows(Exception.class, () -> friendService.getFriends(pageable, ""));

        assertNotNull(exception);
        assertEquals("wrong sort by", exception.getMessage());
    }

    @Test
    public void getFriendRequestSuccess() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageable = PageRequest.of(0, 10, sort);
        Friend friendReq =
                Friend.builder().friendRequester(user1).friendReceiver(user).status(FriendStatusEnum.pending).build();

        when(friendRepository.findFriendRequests(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(friendReq)));

        Page<User> users = friendService.getFriendRequests(pageable);

        assertNotNull(users);
        assertNotNull(users.getContent());
        assertEquals(users.getSize(), 1);
        assertEquals(users.getContent().getFirst(), user1);
    }

    @Test
    public void getFriendRequestFailedWrongSortBy() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "wrong property");
        PageRequest pageable = PageRequest.of(0, 10, sort);

        when(friendRepository.findFriendRequests(anyLong(), any(Pageable.class)))
                .thenThrow(new InvalidDataAccessApiUsageException("wrong sort by"));

        Exception exception = assertThrows(Exception.class, () -> friendService.getFriendRequests(pageable));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "wrong sort by");
    }

    @Test
    public void getFriendRawSuccess() throws Exception {
        when(friendRepository.findAllFriends(anyLong())).thenReturn(List.of(friend1, friend2, friend3));

        List<Friend> friends = friendService.getFriendRaw();

        assertNotNull(friends);
        assertEquals(friends.size(), 3);
        assertEquals(friends, List.of(friend1, friend2, friend3));
    }

}
