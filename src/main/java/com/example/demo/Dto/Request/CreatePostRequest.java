package com.example.demo.Dto.Request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Interface.AtLeastOneFieldNotNull;

import lombok.Data;

@Data
@AtLeastOneFieldNotNull(fields = { "content", "images" }, message = "content or image required")
public class CreatePostRequest {
    private String content;
    private List<MultipartFile> images;
}
