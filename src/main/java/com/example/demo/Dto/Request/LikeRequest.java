package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {
    @NotNull(message = "postId required")
    private Long PostId;
}
