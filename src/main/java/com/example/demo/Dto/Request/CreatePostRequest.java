package com.example.demo.Dto.Request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Interface.AtLeastOneFieldNotNull;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@AtLeastOneFieldNotNull(fields = {"content", "images"}, message = "content or image required")
public class CreatePostRequest {
    @NotNull(message = "user id required")
    private Long userId;

    private String content;

    private List<MultipartFile> images;
}
