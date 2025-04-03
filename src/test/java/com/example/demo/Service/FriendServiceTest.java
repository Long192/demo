package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Friend;
import com.example.demo.Model.User;
import com.example.demo.Repository.FriendRepository;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.UserRepository;

@SpringBootTest
public class FriendServiceTest {
    User user = User.builder().id(1L).email("email@email.com").fullname("fullname").build();
    User user1 = User.builder().id(2L).email("email@email.com").fullname("fullname").build();
    User user2 = User.builder().id(3L).email("email@email.com").fullname("fullname").build();
    User user3 = User.builder().id(4L).email("email@email.com").fullname("fullname").build();
    Friend friend1 = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.accepted)
            .build();
    Friend friend2 = Friend.builder().friendRequester(user).friendReceiver(user2).status(FriendStatusEnum.accepted)
            .build();
    Friend friend3 = Friend.builder().friendRequester(user).friendReceiver(user3).status(FriendStatusEnum.accepted)
            .build();
    @InjectMocks
    private FriendService friendService;
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private PostService postService;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Spy
    private ModelMapper mapper;

    @BeforeEach
    public void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void addFriendSuccess() throws Exception {
        Friend friend =
                Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.pending).build();

        when(userService.findById(user.getId())).thenReturn(user);
        when(userService.findById(user1.getId())).thenReturn(user1);
        when(friendRepository.save(any(Friend.class))).thenReturn(friend);
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
    public void addFriendFailedSelfAdd(){
        CustomException exception = assertThrows(CustomException.class, () -> friendService.addFriend(user.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "can't add yourself");
        assertEquals(exception.getErrorCode(), 404);
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
    public void acceptFriendRequestSuccess() throws Exception {
        Friend pendingFriend = Friend.builder()
                .friendReceiver(user)
                .friendRequester(user1)
                .status(FriendStatusEnum.pending)
                .build();
        Friend addedFriend = Friend.builder()
                .friendReceiver(user)
                .friendRequester(user1)
                .status(FriendStatusEnum.accepted)
                .build();

        when(friendRepository
                .findByFriendRequesterAndFriendReceiverAndStatus(anyLong(), anyLong(), any(FriendStatusEnum.class)))
                .thenReturn(Optional.of(pendingFriend));
        when(friendRepository.save(addedFriend)).thenReturn(addedFriend);

        friendService.acceptFriendRequest(user.getId());

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
        when(friendRepository.findByFriendRequesterAndFriendReceiverAndStatus(1L, 2L,FriendStatusEnum.pending))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> friendService.acceptFriendRequest(user1.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "friend request not found or already friend");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void updateFriendFailedAlreadyFriend() throws Exception {
        when(friendRepository.findByFriendRequesterAndFriendReceiverAndStatus(1L, 2L, FriendStatusEnum.pending))
                .thenReturn(Optional.empty());

        CustomException exception =
                assertThrows(CustomException.class, () -> friendService.acceptFriendRequest(user1.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "friend request not found or already friend");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void removeFriendSuccess() throws Exception {
        when(friendRepository.findByFriendRequesterAndFriendReceiverAndStatus(1L, 2L, FriendStatusEnum.accepted))
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
        Page<Friend> friendPage = new PageImpl<>(List.of(friend1, friend2, friend3));
        UserDto userDto1 = UserDto.builder().fullname("fullname1").email("email1").build();
        UserDto userDto2 = UserDto.builder().fullname("fullname2").email("email2").build();
        UserDto userDto3 = UserDto.builder().fullname("fullname3").email("email3").build();
        CustomPage<UserDto> customPage = new CustomPage<>();
        customPage.setContent(List.of(userDto1, userDto2, userDto3));
        customPage.setPageNumber(1);
        customPage.setTotalPages(1);
        customPage.setTotalElements(3);
        customPage.setSize(3);

        when(friendRepository.findFriends(1L, "", pageable)).thenReturn(friendPage);

        CustomPage<UserDto> users = friendService.getFriends(pageable, "");

        assertNotNull(users);
        assertNotNull(users.getContent());
        assertEquals(users.getSize(), 3);
        assertEquals(users.getContent().size(), 3);
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

        CustomPage<UserDto> users = friendService.getFriendRequests(pageable);

        assertNotNull(users);
        assertNotNull(users.getContent());
        assertEquals(users.getSize(), 1);
        assertEquals(users.getContent().get(0).getId(), user1.getId());
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

    @Test
    public void rejectFriendSuccess() throws Exception {
        Friend friend = Friend.builder()
                .friendRequester(user1)
                .friendReceiver(user)
                .status(FriendStatusEnum.pending)
                .build();

        when(friendRepository.findByFriendRequesterAndFriendReceiverAndStatus(anyLong(), anyLong(),
                any(FriendStatusEnum.class))).thenReturn(Optional.of(friend));

        friendService.deleteFriendRequest(2L);

        ArgumentCaptor<Friend> friendCaptor = ArgumentCaptor.forClass(Friend.class);

        verify(friendRepository, times(1)).delete(friendCaptor.capture());

        Friend friendCaptured = friendCaptor.getValue();

        assertNotNull(friendCaptured);
        assertEquals(friendCaptured, friend);
    }

    @Test
    public void rejectFriendFailedUserNotFound() {
        when(friendRepository.findByFriendRequesterAndFriendReceiverAndStatus(anyLong(), anyLong(),
                any(FriendStatusEnum.class))).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> friendService.deleteFriendRequest(2L));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "friend not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void getAllFriendsSuccess() throws Exception {
        when(friendRepository.findAllFriends(1L)).thenReturn(List.of(friend1, friend2, friend3));

        List<User> users = friendService.getAllFriend();

        assertNotNull(users);
        assertEquals(users.size(), 3);
        assertEquals(users, List.of(user1, user2, user3));
    }

    @Test
    public void getFriendReceiverSuccess() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageable = PageRequest.of(0, 10, sort);
        Page<Friend> friendPage = new PageImpl<>(List.of(friend1, friend2, friend3));
        UserDto userDto1 = UserDto.builder().fullname("fullname1").email("email1").build();
        UserDto userDto2 = UserDto.builder().fullname("fullname2").email("email2").build();
        UserDto userDto3 = UserDto.builder().fullname("fullname3").email("email3").build();
        CustomPage<UserDto> customPage = new CustomPage<>();
        customPage.setContent(List.of(userDto1, userDto2, userDto3));
        customPage.setPageNumber(1);
        customPage.setTotalPages(1);
        customPage.setTotalElements(3);
        customPage.setSize(3);

        when(friendRepository.findFriendReceiver(1L, pageable)).thenReturn(friendPage);

        CustomPage<UserDto> users = friendService.getFriendReceivers(pageable);

        assertNotNull(users);
        assertNotNull(users.getContent());
        assertEquals(users.getSize(), 3);
        assertEquals(users.getContent().size(), 3);
    }

    @Test
    public void getFriendReceiverWrongSortBy() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "fake");
        PageRequest pageable = PageRequest.of(0, 10, sort);

        when(friendRepository.findFriendReceiver(1L, pageable))
                .thenThrow(new InvalidDataAccessApiUsageException("wrong sort by"));

        Exception exception = assertThrows(Exception.class, () -> friendService.getFriendReceivers(pageable));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "wrong sort by");
    }

}
