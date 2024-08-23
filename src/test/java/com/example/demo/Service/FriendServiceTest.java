package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Model.Friend;
import com.example.demo.Model.User;
import com.example.demo.Repository.FriendRepository;
import com.example.demo.Repository.PostRepository;

@SpringBootTest
public class FriendServiceTest {
    @InjectMocks
    private FriendService friendService;
    @Mock
    private FriendRepository friendRepository;
    @Mock
    private PostService postService;
    @Mock
    private PostRepository postRepository;
    @Autowired
    private ModelMapper mapper;

    User user = User.builder().id(1L).email("email@email.com").fullname("fullname").build();
    User user1 = User.builder().id(2L).email("email@email.com").fullname("fullname").build();
    User user2 = User.builder().id(3L).email("email@email.com").fullname("fullname").build();
    User user3 = User.builder().id(4L).email("email@email.com").fullname("fullname").build();

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
        Friend friend1 = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.accepted).build();
        Friend friend2 = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.accepted).build();
        Friend friend3 = Friend.builder().friendRequester(user).friendReceiver(user1).status(FriendStatusEnum.accepted).build();
        PostDto postDto1 = PostDto.builder().content("content").createdAt(timestamp).user(mapper.map(user1, UserDto.class)).build();
        PostDto postDto2 = PostDto.builder().content("content").createdAt(timestamp).user(mapper.map(user2, UserDto.class)).build();
        PostDto postDto3 = PostDto.builder().content("content").createdAt(timestamp).user(mapper.map(user3, UserDto.class)).build();

        when(friendRepository.findAllFriends(user.getId())).thenReturn(List.of(friend1, friend2, friend3));
        when(postService.findPostByUserIdsAndCreatedAt(any(), any(Timestamp.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(postDto1, postDto2, postDto3), pageable, 3));

        Page<PostDto> res = friendService.getFriendPost(pageable);

        assertNotNull(res);
    }
}
