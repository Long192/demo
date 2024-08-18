package com.example.demo.Dto.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @Size(min = 6, message = "password need at least 6 character")
    private String password;

    @NotNull(message = "fullname required")
    private String fullname;

    @NotNull(message = "image required")
    private Object avatar;

    @NotNull(message = "address required")
    private String address;

    @NotNull(message = "etc required")
    private String etc;

    @JsonFormat(pattern = "yyyy/MM/dd")
    @NotNull(message = "dob required")
    private String dob;
}
