package com.example.demo.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Optional;

import com.example.demo.Exception.CustomException;
import com.example.demo.Model.User;
import com.example.demo.Repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.LikeRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Model.Post;
import com.example.demo.Service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    PostDto post1 = PostDto.builder().content("content1").build();
    PostDto post2 = PostDto.builder().content("content2").build();
    PostDto post3 = PostDto.builder().content("content3").build();

    private static String asJsonString(Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    public void getPaginatePostSuccess() throws Exception {
        Page<PostDto> pagePost = new PageImpl<>(Arrays.asList(post1, post2, post3));
        when(postService.findAndPaginate(any(Pageable.class), anyString())).thenReturn(pagePost);
        mockMvc.perform(get("/post").param("page", "0").param("size", "10").param("search", "")
                .param("sortBy", "id").param("order", "asc").contentType(MediaType.APPLICATION_JSON))
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
    public void getPaginatePostFailedWrongSortBy() throws Exception {
        when(postService.findAndPaginate(any(Pageable.class), anyString()))
                .thenThrow(new Exception("cannot find attribute"));
        mockMvc.perform(get("/post").param("page", "0").param("size", "10").param("search", "")
                .param("sortBy", "safasdfasdf").param("order", "asc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("message").value("cannot find attribute"));
    }

    @Test
    @WithMockUser
    public void getMyPostsSuccess() throws Exception {
        Page<PostDto> response = new PageImpl<>(Arrays.asList(post1, post2, post3));
        when(postService.findMyPostsAndPaginate(any(Pageable.class), anyString())).thenReturn(response);
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
        Post post = Post.builder().content("content1").build();
        when(postService.findById(anyLong())).thenReturn(post);
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
        when(postService.findById(anyLong())).thenThrow(new Exception("post not found"));
        mockMvc.perform(get("/post/5")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("post not found"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void createPostSuccess() throws Exception {
        CreatePostRequest req = CreatePostRequest.builder().content("content test").build();
        mockMvc.perform(
                post("/post").contentType(MediaType.APPLICATION_JSON).content(asJsonString(req)))
                .andExpect(status().isOk()).andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.status").value(true))
                .andExpect(jsonPath("data.message").value("success"));
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
    public void createPostSuccessWithFormDataReq() throws Exception {
        MockMultipartFile mockFile1 =
                new MockMultipartFile("images", "img.jpeg", MediaType.IMAGE_JPEG_VALUE, "images".getBytes());
        MockMultipartFile mockFile2 =
                new MockMultipartFile("images", "img.jpeg", MediaType.IMAGE_JPEG_VALUE, "images".getBytes());
        MockMultipartFile mockFile3 =
                new MockMultipartFile("images", "img.jpeg", MediaType.IMAGE_JPEG_VALUE, "images".getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/post").file(mockFile1).file(mockFile2)
                .file(mockFile3).param("content", "test content").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()).andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.message").value("success"))
                .andExpect(jsonPath("data.status").value(true));
    }

    @Test
    @WithMockUser
    public void createPostFailedWithFormDataReqEmpty() throws Exception {
        mockMvc.perform(
                multipart(HttpMethod.POST, "/post").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("status").value(400))
                .andExpect(jsonPath("message").value("content or image required"));
    }

    @Test
    @WithMockUser
    public void likePostSuccess() throws Exception {

        LikeRequest req = LikeRequest.builder().postId(1L).build();

        mockMvc.perform(post("/post/like").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.message").value("success"))
                .andExpect(jsonPath("data.status").value(true));
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
        MockMultipartFile mockFile = new MockMultipartFile("images","img.jpeg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        mockMvc.perform(multipart(HttpMethod.PUT, "/post/1")
                .file(mockFile)
                .param("removeImage[]", "url to remove")
                .param("content", "new content")
                .param("status", "active")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.message").value("success"))
                .andExpect(jsonPath("data.status").value(true));
    }

    @Test
    @WithMockUser
    public void editPostFailedPostNotFound() throws Exception{
        MockMultipartFile mockFile = new MockMultipartFile("images","img.jpeg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        doThrow(new CustomException(404 ,"post not found")).when(postService).editPost(anyLong(),
                any(UpdatePostRequest.class));

        mockMvc.perform(multipart(HttpMethod.PUT, "/post/1")
                .file(mockFile)
                .param("removeImage[]", "url to remove")
                .param("content", "new content")
                .param("status", "active")
                .contentType(MediaType.MULTIPART_FORM_DATA))
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
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.message").value("success"))
                .andExpect(jsonPath("data.status").value(true));
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
