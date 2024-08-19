package com.example.demo.Dto.Request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Validate.Anotations.AtLeastOneFieldNotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AtLeastOneFieldNotNull(fields = {"content", "images"}, message = "content or image required")
public class CreatePostRequest {
    private String content;

    private List<MultipartFile> images;
}
