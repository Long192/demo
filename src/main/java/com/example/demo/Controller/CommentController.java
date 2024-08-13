package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Request.AddCommentRequest;
import com.example.demo.Dto.Response.ApiResponse;
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("")
    public ApiResponse<MessageResponse> postMethodName(@RequestBody @Valid AddCommentRequest request) throws Exception {
        commentService.addComment(request);
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MessageResponse> deleteComment(@PathVariable String id) throws Exception {
        commentService.removeComment(Long.valueOf(id));
        return ApiResponse.<MessageResponse> builder().data(new MessageResponse()).build();
    }
}
