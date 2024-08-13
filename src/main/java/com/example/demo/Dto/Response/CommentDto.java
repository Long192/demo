package com.example.demo.Dto.Response;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private String createdAt;
    private String updatedAt;
}
