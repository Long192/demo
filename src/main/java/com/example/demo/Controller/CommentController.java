package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Request.AddCommentRequest;
import com.example.demo.Dto.Request.UpdateCommentRequest;
import com.example.demo.Dto.Response.CommentDto;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "comment", description = "comments of the posts")
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Operation(summary = "new comment", description = "add new comment for a posts")
    @PostMapping("")
    public ResponseEntity<CustomResponse<CommentDto>> postMethodName(
        @RequestBody @Valid AddCommentRequest request
    ) throws Exception {
        return ResponseEntity.ok(CustomResponse.<CommentDto>builder().data(commentService.addComment(request)).build());
    }

    @Operation(summary = "delete comment", description = "delete a comment")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> deleteComment(@PathVariable Long id) throws Exception {
        commentService.removeComment(id);
        return ResponseEntity.ok(new CustomResponse<>());
    }

    @Operation(summary = "edit comment", description = "edit a comment")
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<CommentDto>> editComment(
        @PathVariable Long id,
        @RequestBody @Valid UpdateCommentRequest req
    ) throws Exception {
        return ResponseEntity.ok(CustomResponse.<CommentDto>builder().data(commentService.editComment(id, req)).build());
    }

}
