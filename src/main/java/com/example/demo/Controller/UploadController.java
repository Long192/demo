package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Request.UploadRequest;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.UploadResponse;
import com.example.demo.Service.UploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "upload", description = "upload")
@RestController
@RequestMapping("/upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @Operation(summary = "upload", description = " image to upload care and get image url")
    @PostMapping(value = "", consumes = {"multipart/form-data"})
    public CustomResponse<UploadResponse> postMethodName(@ModelAttribute UploadRequest req) throws Exception {
        return CustomResponse.<UploadResponse>builder()
                .data(new UploadResponse(uploadService.uploadAndGetUrl(req.getImage())))
                .build();
    }
    
}
