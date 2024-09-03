package com.example.demo.Dto.Request;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonInclude;

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
    @Size(min = 6, max = 50, message = "password between {min} and {max} character")
    @NotNull(message = "password required")
    private String password;
    @Size(max = 50, message = "fullname max {max} character")
    private String fullname;
    @URL(message = "avatar invalid")
    @Size(max = 255, message = "avatar url max {max} character")
    private String avatar;
    @Size(max = 100, message = "address max {max} character")
    private String address;
    @Size(max = 100, message = "etc max {max} character")
    private String etc;
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "dob must be in format yyyy-MM-dd")
    @Size(max = 10, message = "dob max {max} character")
    private String dob;
}
