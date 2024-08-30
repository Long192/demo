package com.example.demo.Dto.Request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 3000, message = "content max {max} characters")
    private String content;
    @NotNull(message = "images required")
    private List<@URL(message = "invalid images url") @Size(max = 255, message = "image url max {max} characters") String> images;
    @NotNull(message = "status required")
    private String status;
}
