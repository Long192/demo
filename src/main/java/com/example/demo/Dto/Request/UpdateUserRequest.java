package com.example.demo.Dto.Request;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @Size(min = 6, message = "password need at least 6 character")
    private String password;
    private String fullname;
    private MultipartFile avatar;
    private String address;
    private String etc;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private String dob;
}
