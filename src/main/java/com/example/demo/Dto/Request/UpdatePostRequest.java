package com.example.demo.Dto.Request;

import com.example.demo.Enum.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UpdatePostRequest {
    @NotNull(message = "content required")
    private String content;

    private List<MultipartFile> images;

    private List<String> removeImages;

//    @NotNull(message = "image required")
//    private List<Object> images;

    @NotNull(message = "content required")
    private StatusEnum status;
}
