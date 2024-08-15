package com.example.demo.Dto.Request;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignUpRequest {
    @Email(message = "wrong email format")
    @NotBlank(message = "email required")
    private String email;
    @NotBlank(message = "password required")
    @Size(min = 6, message = "password need at least 6 character")
    private String password;
    private String fullname;
    private MultipartFile avatar;
    private String address;
    private String etc;
    @JsonFormat(pattern = "yyyy/MM/dd")
    private String dob;
}
