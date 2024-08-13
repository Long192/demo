package com.example.demo.Dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendRequest {
    @NotNull(message = "friendId required")
    private Long friendId;
}
