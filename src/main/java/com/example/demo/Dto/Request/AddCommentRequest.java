package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCommentRequest {
    @NotNull(message = "postId required")
    private Long postId;

    @NotBlank(message = "content required")
    @NotNull(message = "content required")
    private String content;
}
