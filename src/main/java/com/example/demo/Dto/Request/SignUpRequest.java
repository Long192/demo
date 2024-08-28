package com.example.demo.Dto.Request;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Size(max = 50, message = "email max {max} characters")
    @NotBlank(message = "email required")
    private String email;
    @NotNull(message = "password required")
    @Size(min = 6, max = 50, message = "password between {min} and {max} character")
    private String password;
    @Size(max = 50, message = "fullname max {max} character")
    private String fullname;
    private MultipartFile avatar;
    @Size(max = 100, message = "address max {max} character")
    private String address;
    @Size(max = 100, message = "etc max {max} character")
    private String etc;
    @JsonFormat(pattern = "yyyy/MM/dd")
    @Pattern(regexp = "^\\d{4}/\\d{2}/\\d{2}$", message = "Dob must be yyyy/MM/dd format")
    private String dob;
}
