package com.example.demo.Dto.Data;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CacheOtp {
    private String otp;
    private int tryNumber;
    private Long userId;
    private Timestamp expiredAt;
}
