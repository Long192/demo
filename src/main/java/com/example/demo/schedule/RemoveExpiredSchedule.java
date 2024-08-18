package com.example.demo.schedule;

import com.example.demo.Repository.ForgotPasswordRepository;
import com.example.demo.Repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemoveExpiredSchedule {
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;

    @Scheduled(cron = "0 */5 * ? * *")
    public void removeExpired() {
        otpRepository.deleteExPiredOtp();
        forgotPasswordRepository.deleteExpired();
    }
}
