package com.example.demo.Utils;

import java.util.Random;

public class AppUtils {
    public static String generateOtp() {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        int count;
        for (count = 0; count < 4; count++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
