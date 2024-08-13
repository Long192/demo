package com.example.demo.Dto.Request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.Enum.StatusEnum;

import lombok.Data;

@Data
public class UpdatePostRequest {
    private String content;
    private List<MultipartFile> images;
    private List<String> removeImages;
    private StatusEnum status;
}
