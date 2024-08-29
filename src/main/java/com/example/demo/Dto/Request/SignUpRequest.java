package com.example.demo.Dto.Request;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @URL(message = "avatar invalid")
    private String avatar;
    @Size(max = 100, message = "address max {max} character")
    private String address;
    @Size(max = 100, message = "etc max {max} character")
    private String etc;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String dob;
}
