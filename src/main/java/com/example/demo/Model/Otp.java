package com.example.demo.Model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Otp {
    private Long userId;
    private String otp;
    private LocalDateTime expiredAt;
    private int tryNumber;
}
