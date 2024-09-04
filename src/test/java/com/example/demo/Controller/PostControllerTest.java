package com.example.demo.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.LikeRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.CustomPage;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Exception.CustomException;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PostControllerTest {
    PostDto post1 = PostDto.builder().content("content1").build();
    PostDto post2 = PostDto.builder().content("content2").build();
    PostDto post3 = PostDto.builder().content("content3").build();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PostService postService;
    @Mock
    private PostRepository postRepository;
    @Autowired
    private ModelMapper mapper;

    private static String asJsonString(Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    public void getPaginatePostSuccess() throws Exception {
        Page<PostDto> pagePost = new PageImpl<>(Arrays.asList(post1, post2, post3));
        when(postService.getFriendPost(any(Pageable.class)))
                .thenReturn(mapper.map(pagePost, new TypeToken<CustomPage<PostDto>>() {}.getType()));
        mockMvc.perform(get("/post").param("page", "0").param("size", "10").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content[0].content").value("content1"))
                .andExpect(jsonPath("data.content[1].content").value("content2"))
                .andExpect(jsonPath("data.content[2].content").value("content3"));
    }

    @Test
    @WithMockUser
    public void getMyPostsSuccess() throws Exception {
        Page<PostDto> response = new PageImpl<>(Arrays.asList(post1, post2, post3));
        when(postService.findMyPostsAndPaginate(any(Pageable.class), anyString()))
                .thenReturn(mapper.map(response, new TypeToken<CustomPage<PostDto>>() {}.getType()));
        mockMvc.perform(get("/post/my-posts").param("page", "0").param("size", "10")
                .param("search", "").param("sortBy", "id").param("order", "asc")).andExpect(status().isOk())
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content[0].content").value("content1"))
                .andExpect(jsonPath("data.content[1].content").value("content2"))
                .andExpect(jsonPath("data.content[2].content").value("content3"));
    }

    @Test
    @WithMockUser
    public void getMyPostsFailedWrongSortBy() throws Exception {
        when(postService.findMyPostsAndPaginate(any(Pageable.class), anyString()))
                .thenThrow(new InvalidDataAccessApiUsageException("cannot find attribute"));
        mockMvc.perform(get("/post/my-posts").param("page", "0").param("size", "10")
                .param("search", "").param("sortBy", "asdfasdf").param("order", "asc"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("message").value("cannot find attribute"));
    }

    @Test
    @WithMockUser
    public void getPostByIdSuccess() throws Exception {
        PostDto post = PostDto.builder().content("content1").build();
        when(postService.findOneById(anyLong())).thenReturn(post);
        mockMvc.perform(get("/post/1")).andExpect(status().isOk())
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content").value("content1"));
    }

    @Test
    @WithMockUser
    public void getPostByIdFailedIdInvalid() throws Exception {
        mockMvc.perform(get("/post/asdasdfasdf")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("message").value("number format error"));
    }

    @Test
    @WithMockUser
    public void getPostByIdFailedPostNotFound() throws Exception {
        when(postService.findOneById(anyLong())).thenThrow(new Exception("post not found"));
        mockMvc.perform(get("/post/5")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("post not found"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void createPostSuccess() throws Exception {
        CreatePostRequest req = CreatePostRequest.builder().content("content test").build();
        PostDto postDto = mapper.map(req, PostDto.class);

        when(postService.createPost(req)).thenReturn(postDto);

        mockMvc.perform(
                post("/post").contentType(MediaType.APPLICATION_JSON).content(asJsonString(req)))
                .andExpect(status().isOk()).andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").value(postDto));
    }

    @Test
    @WithMockUser
    public void createPostFailedEmtyContentAndImage() throws Exception {
        CreatePostRequest req = new CreatePostRequest();
        mockMvc.perform(
                post("/post").contentType(MediaType.APPLICATION_JSON).content(asJsonString(req)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("message").value("content or image required"));
    }



    @Test
    @WithMockUser
    public void likePostSuccess() throws Exception {

        LikeRequest req = LikeRequest.builder().postId(1L).build();
        PostDto postDto = PostDto.builder().content("content test").build();

        when(postService.like(anyLong())).thenReturn(postDto);

        mockMvc.perform(post("/post/like").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").value(postDto));
    }

    @Test
    @WithMockUser
    public void likePostFailedPostNotFound() throws Exception {

        LikeRequest req = LikeRequest.builder().postId(1L).build();

        doThrow(new CustomException(404, "post not found")).when(postService).like(req.getPostId());

        mockMvc.perform(post("/post/like").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").value(404))
                .andExpect(jsonPath("message").value("post not found"));
    }

    @Test
    @WithMockUser
    public void editPostSuccess() throws Exception{
        UpdatePostRequest req =
                UpdatePostRequest.builder().content("content test").images(List.of()).status("PUBLIC").build();
        PostDto postDto = PostDto.builder().content("content test").build();

        when(postService.editPost(1L, req)).thenReturn(postDto);

        mockMvc.perform(put("/post/1").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").value(postDto));
    }

    @Test
    @WithMockUser
    public void editPostFailedPostNotFound() throws Exception{
        UpdatePostRequest req =
                UpdatePostRequest.builder().content("content test").images(List.of()).status("PUBLIC").build();

        doThrow(new CustomException(404 ,"post not found")).when(postService).editPost(anyLong(),
                any(UpdatePostRequest.class));

        mockMvc.perform(put("/post/1").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("post not found"))
                .andExpect(jsonPath("status").value(404));
    }

    @Test
    @WithMockUser
    public void deletePostSuccess() throws Exception {

        mockMvc.perform(delete("/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"));
    }

    @Test
    @WithMockUser
    public void deletePostFailedPostNotFound() throws Exception {

        doThrow(new CustomException(404, "post not found")).when(postService).deletePostById(anyLong());

        mockMvc.perform(delete("/post/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("status").value(404))
                .andExpect(jsonPath("message").value("post not found"));
    }

    @Test
    @WithMockUser
    public void deletePostFailedPermissionDenied() throws Exception {

        doThrow(new CustomException(403, "you don't have permission to delete this post"))
                .when(postService).deletePostById(anyLong());

        mockMvc.perform(delete("/post/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("status").value(403))
                .andExpect(jsonPath("message").value("you don't have permission to delete this post"));
    }
}
