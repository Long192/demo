package com.example.demo.Dto.Request;

import com.example.demo.Validate.Anotations.AtLeastOneFieldNotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AtLeastOneFieldNotNull(fields = {"content", "images"}, message = "content or image required")
public class CreatePostRequest {
    private String content;

    private List<MultipartFile> images;
}
