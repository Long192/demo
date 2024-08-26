package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddCommentRequest {
    @NotNull(message = "postId required")
    private Long postId;
    @NotBlank(message = "content required")
    @NotNull(message = "content required")
    private String content;
}
