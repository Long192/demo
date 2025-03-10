package com.example.demo.Dto.Request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import com.example.demo.Validate.Anotations.AtLeastOneFieldNotNull;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AtLeastOneFieldNotNull(fields = {"content", "images"}, message = "content or images required")
public class CreatePostRequest {
    @Size(max = 3000, message = "content max {max} characters")
    private String content;
    private List<@URL(message = "invalid images url") String> images;
    private String status;

}
