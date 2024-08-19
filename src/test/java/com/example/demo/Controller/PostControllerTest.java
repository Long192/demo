package com.example.demo.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.hibernate.query.sqm.UnknownPathException;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.demo.Dto.Request.CreatePostRequest;
import com.example.demo.Dto.Request.LikeRequest;
import com.example.demo.Dto.Request.UpdatePostRequest;
import com.example.demo.Dto.Response.PostDto;
import com.example.demo.Model.Post;
import com.example.demo.Service.ImageService;
import com.example.demo.Service.PostService;
import com.example.demo.Service.UserService;
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

    @MockBean
    private ImageService imageService;

    @MockBean
    private UserService userService;
    
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
        mockMvc.perform(MockMvcRequestBuilders.get("/post").param("page", "0").param("size", "10").param("search", "")
                .param("sortBy", "id").param("order", "asc").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.content[0].content").value("content1"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.content[1].content").value("content2"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.content[2].content").value("content3"));
    }

    @Test
    @WithMockUser
    public void getPaginatePostFailedWrongSortBy() throws Exception {
        when(postService.findAndPaginate(any(Pageable.class), anyString()))
                .thenThrow(new UnknownPathException("cannot find attribute"));
        mockMvc.perform(MockMvcRequestBuilders.get("/post").param("page", "0").param("size", "10").param("search", "")
                .param("sortBy", "safasdfasdf").param("order", "asc")).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("cannot find attribute"));
    }

    @Test
    @WithMockUser
    public void getMyPostsSuccess() throws Exception {
        Page<PostDto> response = new PageImpl<>(Arrays.asList(post1, post2, post3));
        when(postService.findMyPostsAndPaginate(any(Pageable.class), anyString())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/post/my-posts").param("page", "0").param("size", "10")
                .param("search", "").param("sortBy", "id").param("order", "asc")).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.content[0].content").value("content1"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.content[1].content").value("content2"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.content[2].content").value("content3"));
    }

    @Test
    @WithMockUser
    public void getMyPostsFailedWrongSortBy() throws Exception {
        when(postService.findMyPostsAndPaginate(any(Pageable.class), anyString()))
                .thenThrow(new InvalidDataAccessApiUsageException("cannot find attribute"));
        mockMvc.perform(MockMvcRequestBuilders.get("/post/my-posts").param("page", "0").param("size", "10")
                .param("search", "").param("sortBy", "asdfasdf").param("order", "asc"))
                .andExpect(status().isBadRequest()).andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("cannot find attribute"));
    }

    @Test
    @WithMockUser
    public void getPostByIdSuccess() throws Exception {
        Post post = Post.builder().content("content1").build();
        when(postService.findById(anyLong())).thenReturn(post);
        mockMvc.perform(MockMvcRequestBuilders.get("/post/1")).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.content").value("content1"));
    }

    @Test
    @WithMockUser
    public void getPostByIdFailedIdInvalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/post/asdasdfasdf")).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("number format error"));
    }

    @Test
    @WithMockUser
    public void getPostByIdFailedPostNotFound() throws Exception {
        when(postService.findById(anyLong())).thenThrow(new Exception("post not found"));
        mockMvc.perform(MockMvcRequestBuilders.get("/post/5")).andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("post not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void createPostSuccess() throws Exception {
        CreatePostRequest req = CreatePostRequest.builder().content("content test").build();
        mockMvc.perform(
                MockMvcRequestBuilders.post("/post").contentType(MediaType.APPLICATION_JSON).content(asJsonString(req)))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.status").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("data.message").value("success"));
    }

    @Test
    @WithMockUser
    public void createPostFailedEmtyContentAndImage() throws Exception {
        CreatePostRequest req = new CreatePostRequest();
        mockMvc.perform(
                MockMvcRequestBuilders.post("/post").contentType(MediaType.APPLICATION_JSON).content(asJsonString(req)))
                .andExpect(status().isBadRequest()).andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("content or image required"));
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
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, "/post").file(mockFile1).file(mockFile2)
                .file(mockFile3).param("content", "test content").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.status").value(true));
    }

    @Test
    @WithMockUser
    public void createPostFailedWithFormDataReqEmpty() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.multipart(HttpMethod.POST, "/post").contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest()).andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("content or image required"));
    }

    @Test
    @WithMockUser
    public void likePostSuccess() throws Exception {

        LikeRequest req = LikeRequest.builder().postId(1L).build();

        mockMvc.perform(MockMvcRequestBuilders.post("/post/like").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.status").value(true));
    }

    @Test
    @WithMockUser
    public void likePostFailedPostNotFound() throws Exception {

        LikeRequest req = LikeRequest.builder().postId(1L).build();

        doThrow(new Exception("post not found")).when(postService).like(req.getPostId());

        mockMvc.perform(MockMvcRequestBuilders.post("/post/like").content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("post not found"));
    }

    @Test
    @WithMockUser
    public void editPostSuccess() throws Exception{
        MockMultipartFile mockFile = new MockMultipartFile("images","img.jpeg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/post/1")
                .file(mockFile)
                .param("removeImage[]", "url to remove")
                .param("content", "new content")
                .param("status", "active")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("data").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("data.message").value("success"))
                .andExpect(MockMvcResultMatchers.jsonPath("data.status").value(true));
    }

    @Test
    @WithMockUser
    public void editPostFailedPostNotFound() throws Exception{
        MockMultipartFile mockFile = new MockMultipartFile("images","img.jpeg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        doThrow(new Exception("post not found")).when(postService).editPost(anyLong(), any(UpdatePostRequest.class));

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/post/1")
                .file(mockFile)
                .param("removeImage[]", "url to remove")
                .param("content", "new content")
                .param("status", "active")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("post not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void editPostFailedIdInvalid() throws Exception{
        MockMultipartFile mockFile = new MockMultipartFile("images","img.jpeg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/post/asdfasdf")
                .file(mockFile)
                .param("removeImage[]", "url to remove")
                .param("content", "new content")
                .param("status", "active")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("wrong number format"))
                .andExpect(MockMvcResultMatchers.jsonPath("status").value(400));
    }

}
