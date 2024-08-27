package com.example.demo.Service;

import com.example.demo.Exception.CustomException;
import com.example.demo.Model.RefreshToken;
import com.example.demo.Model.User;
import com.example.demo.Repository.RefreshRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Ref;
import java.sql.Timestamp;
import java.util.UUID;

@Service
public class RefreshService {

    @Value("${spring.security.expired-refresh}")
    private Long expiredRefresh;

    @Autowired
    private RefreshRepository refreshRepository;

    public String createRefreshToken(User user) {
        RefreshToken refresh =  RefreshToken.builder()
                .refreshToken(UUID.randomUUID().toString())
                .user(user)
                .expiredAt(new Timestamp(System.currentTimeMillis() + expiredRefresh))
                .build();

        RefreshToken result = refreshRepository.save(refresh);

        return result.getRefreshToken();
    }

    public RefreshToken findByUserId(Long id) {
         return refreshRepository.findByUserId(id).orElse(null);
    }
}
