package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.Otp;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Otp findTopByOtpAndUserIdOrderByUserIdDesc(String otp, Long userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Otp WHERE expiredAt < CURRENT_TIMESTAMP")
    void deleteExPiredOtp();
}
