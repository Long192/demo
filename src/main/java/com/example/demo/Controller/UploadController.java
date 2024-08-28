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


@RestController
@RequestMapping("/upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @PostMapping(value = "", consumes = {"multipart/form-data"})
    public CustomResponse<UploadResponse> postMethodName(@ModelAttribute UploadRequest req) throws Exception {
        return CustomResponse.<UploadResponse>builder()
                .data(new UploadResponse(uploadService.uploadAndGetUrl(req.getImage())))
                .build();
    }
    
}
