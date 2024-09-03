package com.example.demo.Dto.Request;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Pattern;
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
    @Size(max = 50, message = "fullname max {max} character")
    private String fullname;
    @URL(message = "url invalid")
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
