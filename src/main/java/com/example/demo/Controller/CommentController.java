package com.example.demo.Controller;

import com.example.demo.Dto.Request.AddCommentRequest;
import com.example.demo.Dto.Request.UpdateCommentRequest;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "comment", description = "comments of the posts")
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Operation(summary = "new comment", description = "add new comment for a posts")
    @PostMapping("")
    public ResponseEntity<CustomResponse<MessageResponse>> postMethodName(
        @RequestBody @Valid AddCommentRequest request
    ) throws Exception {
        commentService.addComment(request);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }

    @Operation(summary = "delete comment", description = "delete a comment")
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<MessageResponse>> deleteComment(@PathVariable Long id) throws Exception {
        commentService.removeComment(id);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }

    @Operation(summary = "edit comment", description = "edit a comment")
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<MessageResponse>> editComment(
        @PathVariable Long id,
        @ModelAttribute @Valid UpdateCommentRequest req
    ) throws Exception {
        commentService.editComment(id, req);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }

}
