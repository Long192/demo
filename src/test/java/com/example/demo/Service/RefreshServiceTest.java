package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.Exception.CustomException;
import com.example.demo.Model.RefreshToken;
import com.example.demo.Model.User;
import com.example.demo.Repository.RefreshRepository;

@SpringBootTest
class RefreshServiceTest {
    @InjectMocks
    private RefreshService refreshService;
    @Mock
    private RefreshRepository refreshRepository;

    UUID mockUUID = UUID.randomUUID();
    MockedStatic<UUID> mockedUUID;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(refreshService, "expiredRefresh", 86400000L);
        mockedUUID = mockStatic(UUID.class);
    }

    @Test
    public void createRefreshToken() {
        User user = User.builder()
                .email("email@email.com")
                .password("password")
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token(mockedUUID.toString())
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
                .build();

        mockedUUID.when(UUID::randomUUID).thenReturn(mockUUID);
        when(refreshRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        String res = refreshService.createRefreshToken(user);

        assertNotNull(res);
    }

    @Test
    public void findByTokenSuccess() throws Exception {
        User user = User.builder()
                .email("email@email.com")
                .password("password")
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token(mockedUUID.toString())
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
                .build();

        when(refreshRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        RefreshToken res = refreshService.findByToken(mockedUUID.toString());

        assertNotNull(res);
        assertEquals(refreshToken.getToken(), res.getToken());
    }

    @Test
    public void findByTokenFailed() throws Exception {
        when(refreshRepository.findByToken(anyString())).thenReturn(Optional.empty());

        CustomException exception =
                assertThrows(CustomException.class, () -> refreshService.findByToken(mockedUUID.toString()));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "token invalid");
    }

    @Test
    public void findByUserIdSuccess() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token(mockedUUID.toString())
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
                .build();

        when(refreshRepository.findByUserId(anyLong())).thenReturn(Optional.of(refreshToken));

        RefreshToken res = refreshService.findByUserId(1L);

        assertNotNull(res);
        assertEquals(res, refreshToken);
    }

    @Test
    public void findByUserIdNullResult() throws Exception {
        when(refreshRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        RefreshToken res = refreshService.findByUserId(1L);

        assertNull(res);
    }

    @AfterEach
    public void tearDown() {
        mockedUUID.close();
    }
}