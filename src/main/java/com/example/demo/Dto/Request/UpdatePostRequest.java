package com.example.demo.Dto.Request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Enum.StatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePostRequest {
    @NotNull(message = "content required")
    private String content;

    private List<MultipartFile> images;

    private List<String> removeImages;

//    @NotNull(message = "image required")
//    private List<Object> images;

    @NotNull(message = "status required")
    private StatusEnum status;
}
