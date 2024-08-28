package com.example.demo.Dto.Request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadRequest {
    @NotNull(message = "image require")
    private MultipartFile image;
}
