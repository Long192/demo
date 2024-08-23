package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Enum.StatusEnum;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Favourite;
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
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private FavouriteService favouriteService;

    User user = User.builder().id(1L).email("email@email.com").build();
    User user2 = User.builder().id(2L).email("email@email.com").build();

    Post post1 = Post.builder().content("content1").user(user).build();
    Post post2 = Post.builder().content("content2").user(user).build();
    Post post3 = Post.builder().content("content3").user(user).build();
    Post post4 = Post.builder().content("content4").user(user2).build();

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
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        CreatePostRequest req = CreatePostRequest.builder().images(List.of(file)).content("content").build();
        User user = User.builder().id(1L).email("email@email.com").fullname("fullname").build();
        Post post = Post.builder()
                .user(user)
                .content("content")
                .build();

        postService.createPost(req);
        
        ArgumentCaptor<Post> postCaptured = ArgumentCaptor.forClass(Post.class);

        verify(postRepository, atLeastOnce()).save(postCaptured.capture());
        Post postCaptor = postCaptured.getValue();

        verify(imageService, atLeastOnce()).upload(List.of(file), postCaptor);

        assertNotNull(postCaptor);
        assertEquals(postCaptor.getContent(), post.getContent());
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

        postService.createPost(req);

        ArgumentCaptor<Post> postCaptured = ArgumentCaptor.forClass(Post.class);

        verify(postRepository, atLeastOnce()).save(postCaptured.capture());
        verify(imageService, never()).upload(any(), any(Post.class));
        Post postCaptor = postCaptured.getValue();


        assertNotNull(postCaptor);
        assertEquals(postCaptor.getContent(), post.getContent());
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
    public void findPostByUserIdsAndCreatedAtSuccess() throws Exception {
        Page<Post> posts = new PageImpl<>(List.of(post1, post2, post3));
        Page<PostDto>postDtos = posts.map(source -> modelMapper.map(source, PostDto.class));
        List<Long> ids = List.of(1L, 2L, 3L);
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        PageRequest page = PageRequest.of(0, 10);

        when(postRepository.findPostByUserIdsAndCreatedAt(ids, createdAt, page)).thenReturn(posts);

        Page<PostDto> res = postService.findPostByUserIdsAndCreatedAt(ids, createdAt, page);

        assertNotNull(res);
        assertEquals(res, postDtos);
    }

    @Test
    public void findAndPaginateSuccess() throws Exception {
        Page<Post> posts = new PageImpl<>(List.of(post1, post2, post3));
        Page<PostDto>postDtos = posts.map(source -> modelMapper.map(source, PostDto.class));
        PageRequest page = PageRequest.of(0, 10);

        when(postRepository.findPostWithSearchAndSort("search", StatusEnum.active, page)).thenReturn(posts);

        Page<PostDto> res = postService.findAndPaginate(page, "search");

        assertNotNull(res);
        assertEquals(postDtos, res);
    }

    @Test
    public void findAndPaginateFailed() throws Exception {
        PageRequest page = PageRequest.of(0, 10);
        when(postRepository.findPostWithSearchAndSort("search", StatusEnum.active, page))
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

        Page<PostDto> res = postService.findMyPostsAndPaginate(page, "search");

        assertNotNull(res);
        assertEquals(postDtos, res);
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
        MockMultipartFile file =
                new MockMultipartFile("images", "images.jpeg", MediaType.IMAGE_JPEG_VALUE, "images".getBytes());
        UpdatePostRequest req = UpdatePostRequest.builder()
                .content("content")
                .images(List.of(file))
                .build();
        Image img = Image.builder().id(1L).url("url").build();

        when(imageService.editImage(List.of(file), post1, null)).thenReturn(List.of(img));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        postService.editPost(1L, req);

        ArgumentCaptor<Post> postCaptured = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postCaptured.capture());

        Post postCaptor = postCaptured.getValue();

        assertNotNull(postCaptor);
        assertEquals(post1, postCaptor);
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
        UpdatePostRequest req = UpdatePostRequest.builder().content(null).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

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

        ArgumentCaptor<Favourite> favouriteCaptured = ArgumentCaptor.forClass(Favourite.class);

        postService.like(1L);

        verify(favouriteService, atLeastOnce()).save(favouriteCaptured.capture());

        Favourite favouriteCaptor = favouriteCaptured.getValue();

        assertNotNull(favouriteCaptor);
        assertEquals(favourite, favouriteCaptor);
    }

    @Test
    public void dissLikeSuccess() throws Exception {
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
    public void deletePostFailedPermissionDenied() throws Exception {
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post4));

        CustomException exception = assertThrows(CustomException.class, () -> postService.deletePostById(anyLong()));

        assertEquals(exception.getErrorCode(), 403);
        assertEquals(exception.getMessage(), "you don't have permission to delete this post");
    }

}
