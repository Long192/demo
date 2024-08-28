package com.example.demo.Service;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.Exception.CustomException;
import com.example.demo.Model.RefreshToken;
import com.example.demo.Model.User;
import com.example.demo.Repository.RefreshRepository;

@Service
public class RefreshService {

    @Value("${spring.security.expired-refresh}")
    private Long expiredRefresh;

    @Autowired
    private RefreshRepository refreshRepository;

    public String createRefreshToken(User user) {
        RefreshToken refresh =  RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiredAt(new Timestamp(System.currentTimeMillis() + expiredRefresh))
                .build();

        RefreshToken result = refreshRepository.save(refresh);

        return result.getToken();
    }

    public RefreshToken findByToken(String token) throws Exception {
        return refreshRepository.findByToken(token).orElseThrow(() -> new CustomException(404, "token invalid"));
    }

    public RefreshToken findByUserId(Long id) {
         return refreshRepository.findByUserId(id).orElse(null);
    }
}
