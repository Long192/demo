package com.example.demo.Repository;

import com.example.demo.Model.ForgotPassword;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Long> {
    ForgotPassword findByUserIdAndToken(Long id, String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM ForgotPassword WHERE expiredAt < CURRENT_TIMESTAMP")
    void deleteExpired();
}
