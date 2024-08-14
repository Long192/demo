package com.example.demo.Dto.Response;

import java.util.List;

import com.example.demo.Enum.StatusEnum;

import lombok.Data;

@Data
public class PostDto {
    private Long id;
    private UserDto user;
    private String content;
    private StatusEnum status;
    private List<UserDto> likedByUsers;
    private List<CommentDto> comments;
    private List<ImageDto> images;
    private String createdAt;
    private String updatedAt;
}
