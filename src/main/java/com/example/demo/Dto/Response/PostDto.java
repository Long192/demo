package com.example.demo.Dto.Response;

import java.sql.Timestamp;
import java.util.List;

import com.example.demo.Enum.PostStatusEnum;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDto {
    private Long id;
    private UserDto user;
    private String content;
    private PostStatusEnum status;
    private List<UserDto> likedByUsers;
    private List<CommentDto> comments;
    private List<ImageDto> images;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
