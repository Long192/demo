package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Enum.FriendStatusEnum;
import com.example.demo.Enum.PostStatusEnum;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Favourite;
import com.example.demo.Model.Friend;
import com.example.demo.Model.Image;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.ImageRepository;
import com.example.demo.Repository.PostRepository;

@SpringBootTest
public class PostServiceTest {
    @InjectMocks
    private PostService postService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private UserService userService;
    @Spy
    private ModelMapper modelMapper;
    @Spy
    private ModelMapper mapper;
    @Mock
    private FavouriteService favouriteService;
    @Mock
    private FriendService friendService;

    User user = User.builder().id(1L).email("email@email.com").build();
    User user2 = User.builder().id(2L).email("email@email.com").likePosts(List.of()).build();

    Post post1 = Post.builder()
            .content("content1")
            .user(user).likedByUsers(List.of())
            .images(List.of()).status(PostStatusEnum.PUBLIC)
            .build();
    Post post2 = Post.builder()
            .content("content2")
            .user(user)
            .status(PostStatusEnum.PUBLIC)
            .build();
    Post post3 = Post.builder()
            .content("content3")
            .user(user)
            .status(PostStatusEnum.PUBLIC)
            .build();
    Post post4 = Post.builder()
            .content("content4")
            .user(user2)
            .status(PostStatusEnum.PUBLIC)
            .build();

    @BeforeEach
    public void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @Transactional
    public void createPostSuccess() throws Exception {
        CreatePostRequest req = CreatePostRequest.builder().images(List.of("url")).content("content").build();
        User user = User.builder().id(1L).email("email@email.com").fullname("fullname").build();
        Image img = Image.builder().id(1L).url("url").build();
        Post post = Post.builder()
                .user(user)
                .content("content")
                .images(List.of(img))
                .build();
        img.setPost(post);

        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostDto res = postService.createPost(req);

        assertNotNull(res);
        assertEquals(res.getContent(), post.getContent());
    }

    @Test
    public void createdPostFailedStatusInvalid() {
        CreatePostRequest req =
                CreatePostRequest.builder().images(List.of("url")).status("fake").content("content").build();

        Exception exception = assertThrows(Exception.class, () -> postService.createPost(req));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "status only accept 'PRIVATE', 'PUBLIC' or 'FRIEND_ONLY'");
    }

    @Test
    @Transactional
    public void createPostSuccessWithImagesEmpty() throws Exception { 
        CreatePostRequest req = CreatePostRequest.builder().images(List.of()).content("content").build();
        User user = User.builder().id(1L).email("email@email.com").fullname("fullname").build();
        Post post = Post.builder()
                .user(user)
                .content("content")
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostDto res = postService.createPost(req);

        assertNotNull(res);
        assertEquals(res.getContent(), post.getContent());
    }

    @Test
    @Transactional
    public void createPostSuccessWithoutImages() throws Exception {
        CreatePostRequest req = CreatePostRequest.builder().content("content").build();
        User user = User.builder().id(1L).email("email@email.com").fullname("fullname").build();
        Post post = Post.builder()
                .user(user)
                .content("content")
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostDto res = postService.createPost(req);

        assertNotNull(res);
        assertEquals(res.getContent(), post.getContent());
    }

    @Test
    @Transactional
    public void createPostFailedCannotSaveData() throws Exception {
        CreatePostRequest req = CreatePostRequest.builder().content("content").build();
                
        doThrow(new RuntimeException("entity already exists")).when(postRepository).save(any(Post.class));
        
        Exception exception = assertThrows(Exception.class, () -> postService.createPost(req));

        assertEquals(exception.getMessage(), "entity already exists");
    }


    @Test
    public void findAndPaginateSuccess() throws Exception {
        Page<Post> posts = new PageImpl<>(List.of(post1, post2, post3));
        Page<PostDto>postDtos = posts.map(source -> modelMapper.map(source, PostDto.class));
        PageRequest page = PageRequest.of(0, 10);

        when(postRepository.findPostWithSearchAndSort("search", PostStatusEnum.PUBLIC, page)).thenReturn(posts);

        CustomPage<PostDto> res = postService.findAndPaginate(page, "search");

        assertNotNull(res);
        assertEquals(postDtos.getContent().getFirst().getId(), res.getContent().getFirst().getId());
    }

    @Test
    public void findAndPaginateFailed() throws Exception {
        PageRequest page = PageRequest.of(0, 10);
        when(postRepository.findPostWithSearchAndSort("search", PostStatusEnum.PUBLIC, page))
                .thenThrow(new InvalidDataAccessApiUsageException(null));

        Exception exception =
                assertThrows(Exception.class, () -> postService.findAndPaginate(page, "search"));

        assertEquals(exception.getMessage(), "wrong sort by");
    }

    @Test
    public void findMyPostSuccess() throws Exception {
        Page<Post> posts = new PageImpl<>(List.of(post1, post2, post3));
        Page<PostDto>postDtos = posts.map(source -> modelMapper.map(source, PostDto.class));
        PageRequest page = PageRequest.of(0, 10);

        when(postRepository.getPostByUserIdAndSearch(1L, page, "search")).thenReturn(posts);

        CustomPage<PostDto> res = postService.findMyPostsAndPaginate(page, "search");

        assertNotNull(res);
        assertEquals(postDtos.getContent().getFirst().getId(), res.getContent().getFirst().getId());
    }

    @Test
    public void findMyPostFailedWrongSortBy() throws Exception {
        PageRequest page = PageRequest.of(0, 10);
        when(postRepository.getPostByUserIdAndSearch(1L, page, "search"))
                .thenThrow(new InvalidDataAccessApiUsageException(null));

        Exception exception =
                assertThrows(Exception.class, () -> postService.findMyPostsAndPaginate(page, "search"));

        assertEquals(exception.getMessage(), "wrong sort by");
    }

    @Test
    public void findByIdSuccess() throws Exception {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        Post res = postService.findById(1L);

        assertNotNull(res);
        assertEquals(res, post1);
    }

    @Test
    public void findByIdFailed() throws Exception {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> postService.findById(1L));

        assertEquals(exception.getMessage(), "post not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    @Transactional
    public void editPostSuccess() throws Exception {
        Post testPost = Post.builder().id(1L).user(user).content("content").build();
        UpdatePostRequest req = UpdatePostRequest.builder()
                .content("content")
                .images(List.of("url"))
                .status("PUBLIC")
                .build();
        Image img = Image.builder().id(1L).url("url").post(post1).build();
        testPost.setImages(new ArrayList<>());
        testPost.getImages().add(img);

        when(imageService.editImage(List.of("url"), post1)).thenReturn(List.of(img));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        postService.editPost(1L, req);

        ArgumentCaptor<Post> postCaptured = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postCaptured.capture());

        Post postCaptor = postCaptured.getValue();

        assertNotNull(postCaptor);
        assertEquals(testPost, postCaptor);
    }

    @Test
    @Transactional
    public void editPostFailedWrongInvalid() throws Exception {
        Post testPost = Post.builder().id(1L).user(user).content("content").build();
        UpdatePostRequest req = UpdatePostRequest.builder()
                .content("content")
                .images(List.of("url"))
                .status("status")
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        Exception exception = assertThrows(Exception.class, () -> postService.editPost(1L, req));
        assertEquals(exception.getMessage(), "status only accept 'PRIVATE', 'PUBLIC' or 'FRIEND_ONLY'");
    }

    @Test
    @Transactional
    public void editPostFailedPostNotFound() throws Exception {
        UpdatePostRequest req = UpdatePostRequest.builder()
                .content("content")
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> postService.editPost(1L, req));

        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "post not found");
    }

    @Test
    @Transactional
    public void editPostFailedDontHavePermission() throws Exception {
        UpdatePostRequest req = UpdatePostRequest.builder().content("new content").build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post4));

        CustomException exception = assertThrows(CustomException.class, () -> postService.editPost(1L, req));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "you don't have permission to edit this post");
        assertEquals(exception.getErrorCode(), 403);
    }

    @Test
    @Transactional
    public void editPostFailedDeleteAllContentAndImage() throws Exception {
        Post testPost = Post.builder().id(1L).user(user).content("content").build();
        Image img = Image.builder().id(1L).url("url").post(post1).build();
        testPost.setImages(new ArrayList<>());
        testPost.getImages().add(img);
        UpdatePostRequest req = UpdatePostRequest.builder().content(null).status("PUBLIC").images(List.of()).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        Exception exception = assertThrows(Exception.class, () -> postService.editPost(1L, req));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "cannot delete all content and image");
    }

    @Test
    public void likeSuccess() throws Exception {
        Favourite favourite = Favourite.builder().user(user).post(post1).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(userService.findById(1L)).thenReturn(user);
        when(favouriteService.findById(anyLong())).thenReturn(Optional.empty());
        when(favouriteService.save(any(Favourite.class))).thenReturn(favourite);

        PostDto res = postService.like(1L);

        assertNotNull(res);
        assertEquals(favourite.getPost().getId(), res.getId());
    }

    @Test
    public void disLikeSuccess() throws Exception {
        Favourite favourite = Favourite.builder().id(1L).user(user).post(post1).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(userService.findById(1L)).thenReturn(user);
        when(favouriteService.findByUserIdAndPostId(user.getId(), post1.getId())).thenReturn(Optional.of(favourite));

        postService.like(1L);
        
        ArgumentCaptor<Favourite> favouriteCaptor  = ArgumentCaptor.forClass(Favourite.class);
        verify(favouriteService, atLeastOnce()).delete(favouriteCaptor.capture());
        
        Favourite favouriteCaptured = favouriteCaptor.getValue();

        assertNotNull(favouriteCaptured);
        assertEquals(favourite, favouriteCaptured);

    }

    @Test
    public void likeFailedUserNotFound() throws Exception {
        when(userService.findById(anyLong())).thenThrow(new CustomException(404, "user not found"));

        CustomException exception = assertThrows(CustomException.class, () -> postService.like(1L));

        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "user not found");
    }

    @Test
    public void likeFailedPostNotFound() throws Exception {
        when(userService.findById(anyLong())).thenReturn(user);
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> postService.like(1L));

        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "post not found");
    }

    @Test
    @Transactional
    public void deletePostSuccess() throws Exception {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post1));

        postService.deletePostById(1L);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository, times(1)).delete(postCaptor.capture());

        Post postCaptured = postCaptor.getValue();

        assertNotNull(postCaptured);
        assertEquals(postCaptured, post1);
    }

    @Test
    @Transactional
    public void deletePostSuccessWithLike() throws Exception {
        User testUser = User.builder().id(2L).email("email@email.com").likePosts(List.of()).build();
        Post testPost =
                Post.builder().content("content1").user(user).likedByUsers(new ArrayList<>(List.of(testUser))).build();
        testUser.setLikePosts(new ArrayList<>(List.of(testPost)));

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

        postService.deletePostById(1L);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository, times(1)).delete(postCaptor.capture());

        Post postCaptured = postCaptor.getValue();

        assertNotNull(postCaptured);
        assertEquals(postCaptured, testPost);
    }

    @Test
    public void deletePostFailedPermissionDenied() throws Exception {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post4));

        CustomException exception = assertThrows(CustomException.class, () -> postService.deletePostById(anyLong()));

        assertEquals(exception.getErrorCode(), 403);
        assertEquals(exception.getMessage(), "you don't have permission to delete this post");
    }

    @Test
    public void getFriendPostSuccess() throws Exception {
        Page<Post> posts = new PageImpl<>(List.of(post1, post2, post3));

        when(friendService.getAllFriend()).thenReturn(List.of(user, user2));
        when(postRepository.findByUserIdsOrderByCreatedAt(any(), any(), any(Pageable.class))).thenReturn(posts);

        CustomPage<PostDto> res = postService.getFriendPost(PageRequest.of(0, 10));

        assertNotNull(res);
        assertEquals(res.getContent().size(), 3);
        assertEquals(res.getTotalElements(), 3);
    }

    @Test
    public void findOneByIdSuccess() throws Exception {
        Post testPost = Post.builder()
                .id(1L)
                .content("content1")
                .user(user)
                .status(PostStatusEnum.PUBLIC)
                .build();

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

        Post res = postService.findOneById(1L);

        assertNotNull(res);
        assertEquals(res.getContent(), post1.getContent());
    }

    @Test
    public void findOneByIdStatusFriendSuccess() throws Exception {
        Post testPost = Post.builder()
                .id(1L)
                .content("content1")
                .user(user2)
                .status(PostStatusEnum.FRIEND_ONLY)
                .build();

        Friend friend = Friend.builder().id(1L).friendReceiver(user).friendRequester(user2).build();

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(friendService.findByFriendRequesterAndFriendReceiverAndStatus(anyLong(), anyLong(), any(FriendStatusEnum.class))).thenReturn(friend);

        Post res = postService.findOneById(1L);

        assertNotNull(res);
        assertEquals(res.getContent(), testPost.getContent());
    }

    @Test
    public void findOneByIdStatusFriendFailed() throws Exception {
        Post testPost = Post.builder()
                .id(1L)
                .content("content1")
                .user(user2)
                .status(PostStatusEnum.FRIEND_ONLY)
                .build();


        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));
        when(friendService.findByFriendRequesterAndFriendReceiverAndStatus(anyLong(), anyLong(), any(FriendStatusEnum.class))).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> postService.findOneById(1L));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 403);
        assertEquals(exception.getMessage(), "you don't have permission to access this post");
    }

    @Test
    public void findOneByIdFailedStatusPrivate() throws Exception {
        Post testPost = Post.builder()
                .id(1L)
                .content("content1")
                .user(user2)
                .status(PostStatusEnum.PRIVATE)
                .build();

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

        CustomException exception = assertThrows(CustomException.class, () -> postService.findOneById(1L));

        assertEquals(exception.getErrorCode(), 403);
        assertEquals(exception.getMessage(), "you don't have permission to access this post");
    }

    @Test
    public void findOneByIdSuccesstatusPrivate() throws Exception {
        Post testPost = Post.builder()
                .id(1L)
                .content("content1")
                .user(user)
                .status(PostStatusEnum.PRIVATE)
                .build();

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(testPost));

        Post res = postService.findOneById(1L);

        assertNotNull(res);
        assertEquals(res.getContent(), testPost.getContent());
    }
}
