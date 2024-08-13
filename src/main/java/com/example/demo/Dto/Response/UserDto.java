package com.example.demo.Dto.Response;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String address;
    private String fullname;
    private String etc;
    private String avatar;
    private String dob;
}
