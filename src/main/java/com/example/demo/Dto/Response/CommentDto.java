package com.example.demo.Dto.Response;

import com.example.demo.Model.Post;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private Post post;
    private String createdAt;
    private String updatedAt;
}
