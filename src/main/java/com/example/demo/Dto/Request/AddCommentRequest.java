package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 3000, message = "content max {max} characters")
    private String content;
}
