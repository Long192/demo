package com.example.demo.Controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.Dto.Response.CommentDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Dto.Request.AddCommentRequest;
import com.example.demo.Dto.Request.UpdateCommentRequest;
import com.example.demo.Service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;

    private static String asJsonString(final Object obj) throws Exception {
        return new ObjectMapper().writeValueAsString(obj);
    }

    @Test
    @WithMockUser
    public void addCommentSuccess() throws Exception {
        AddCommentRequest req = AddCommentRequest.builder().content("comment 1").postId(1L).build();

        when(commentService.addComment(req)).thenReturn(CommentDto.builder().id(1L).content(req.getContent()).build());

        mockMvc.perform(post("/comment").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content").value(req.getContent()));
    }

    @Test
    @WithMockUser
    public void addCommentFailedContentEmpty() throws Exception {
        AddCommentRequest req = AddCommentRequest.builder().content("").postId(1L).build();

        mockMvc.perform(post("/comment")
                .content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("content required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void addCommentFailedContentNull() throws Exception {
        AddCommentRequest req = AddCommentRequest.builder().content(null).postId(1L).build();

        mockMvc.perform(post("/comment")
                .content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("content required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void addCommentFailedUserIdNull() throws Exception {
        AddCommentRequest req = AddCommentRequest.builder().content("content").postId(null).build();

        mockMvc.perform(post("/comment")
                .content(asJsonString(req))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("postId required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void deleteCommentSuccess() throws Exception {

        mockMvc.perform(delete("/comment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200));
    }

    @Test
    @WithMockUser
    public void deleteCommentFailedCommentNotFound() throws Exception{

        doThrow(new Exception("comment not found")).when(commentService).removeComment(anyLong());

        mockMvc.perform(delete("/comment/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("comment not found"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void deleteCommentFailedDontHavePermission() throws Exception {

        doThrow(new Exception("cannot delete this comment")).when(commentService).removeComment(anyLong());

        mockMvc.perform(delete("/comment/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("cannot delete this comment"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void updateCommentSuccess() throws Exception {

        UpdateCommentRequest req = UpdateCommentRequest.builder().content("test").build();

        when(commentService.editComment(1L, req))
                .thenReturn(CommentDto.builder().id(1L).content(req.getContent()).build());

        mockMvc.perform(put("/comment/1").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data").exists())
                .andExpect(jsonPath("data.content").value(req.getContent()));
    }

    @Test
    @WithMockUser
    public void updateCommentFailedContentEmpty() throws Exception {
        UpdateCommentRequest req = UpdateCommentRequest.builder().content("").build();

        mockMvc.perform(put("/comment/1").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("content required"))
                .andExpect(jsonPath("status").value(400));
    }

    @Test
    @WithMockUser
    public void updateCommentFailedContentNull() throws Exception {
        UpdateCommentRequest req = UpdateCommentRequest.builder().content(null).build();

        mockMvc.perform(put("/comment/1").content(asJsonString(req))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("content required"))
                .andExpect(jsonPath("status").value(400));
    }
}
