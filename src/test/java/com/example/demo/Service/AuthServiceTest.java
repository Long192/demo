package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.Dto.Request.GetTokenRequest;
import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Dto.Response.ForgotPasswordResponse;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.ForgotPassword;
import com.example.demo.Model.Otp;
import com.example.demo.Model.RefreshToken;
import com.example.demo.Model.User;
import com.example.demo.Repository.RefreshRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Utils.AppUtils;
import com.example.demo.Utils.JwtUtil;

@SpringBootTest
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshService refreshService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private CacheManager cacheManager;
    @Spy
    private ModelMapper mapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private Environment environment;
    @Mock
    private RefreshRepository refreshRepository;


    UUID mockUUID = UUID.randomUUID();

    MockedStatic<AppUtils> mockedStatic;
    MockedStatic<UUID> uuidMockedStatic;

    @BeforeEach
    public void setup() {
        uuidMockedStatic = mockStatic(UUID.class);
        mockedStatic = mockStatic(AppUtils.class);
    }

    @Test
    public void SignupTestSuccess() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SignUpRequest req = SignUpRequest.builder().email("email@testemail.com").password("password").etc("etc")
                .address("address").fullname("fullname").dob("2002-01-09").build();
        User user = User.builder().email(req.getEmail()).password(req.getPassword()).etc(req.getEtc())
                .address(req.getAddress()).fullname(req.getFullname())
                .dob(new Date(format.parse(req.getDob()).getTime())).build();

        when(passwordEncoder.encode(req.getPassword())).thenReturn("password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto res = authService.signUp(req);

        assertNotNull(res);
        assertEquals(user.getEmail(), res.getEmail());
        assertEquals(user.getAddress(), res.getAddress());
        assertEquals(user.getDob().toString(), res.getDob());
        assertEquals(user.getEtc(), res.getEtc());
        assertEquals(user.getFullname(), res.getFullname());
    }

    @Test
    public void SignupTestFailureWrongDobFormat() throws Exception {
        SignUpRequest req = SignUpRequest.builder()
                .email("email@email.com")
                .password("password")
                .dob("2002/01/09")
                .build();

        CustomException exception = assertThrows(CustomException.class, () -> authService.signUp(req));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "Dob invalid");
        assertEquals(exception.getErrorCode(), 400);
    }

    @Test
    public void SignupTestFailureDobIsAfter() throws Exception {
        SignUpRequest req = SignUpRequest.builder()
                .email("email@email.com")
                .password("password")
                .dob("2030-01-09")
                .build();

        CustomException exception = assertThrows(CustomException.class, () -> authService.signUp(req));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "Dob invalid");
        assertEquals(exception.getErrorCode(), 400);
    }

    @Test
    public void SignupTestSuccessWithAvatar() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@testemail.com").password("password").etc("etc")
                .address("address").fullname("fullname").avatar("url").build();
        User user = User.builder().email(req.getEmail()).password(req.getPassword()).etc(req.getEtc())
                .address(req.getAddress()).fullname(req.getFullname()).build();

        when(passwordEncoder.encode(req.getPassword())).thenReturn("password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto res = authService.signUp(req);

        assertEquals(user.getEmail(), res.getEmail());
        assertEquals(user.getAddress(), res.getAddress());
        assertEquals(user.getDob(), res.getDob());
        assertEquals(user.getEtc(), res.getEtc());
        assertEquals(user.getFullname(), res.getFullname());
    }

    @Test
    public void SignupTestSuccessDobBlank() throws Exception {
        SignUpRequest req = SignUpRequest.builder().email("email@testemail.com").password("password").etc("etc")
                .address("address").fullname("fullname").avatar("url").dob("").build();
        User user = User.builder().email(req.getEmail()).password(req.getPassword()).etc(req.getEtc())
                .address(req.getAddress()).fullname(req.getFullname()).build();

        when(passwordEncoder.encode(req.getPassword())).thenReturn("password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto res = authService.signUp(req);

        assertEquals(user.getEmail(), res.getEmail());
        assertEquals(user.getAddress(), res.getAddress());
        assertEquals(user.getDob(), res.getDob());
        assertEquals(user.getEtc(), res.getEtc());
        assertEquals(user.getFullname(), res.getFullname());
    }

    @Test
    public void LoginOtpSuccess() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("email@email.com")
                .password("password")
                .build();
        User user = User.builder().id(1L).email("email@email.com").build();
        Cache cache = mock(Cache.class);

        when(cacheManager.getCache("otpCache")).thenReturn(cache);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        mockedStatic.when(AppUtils::generateOtp).thenReturn("3214");

        OtpDto otpDto = authService.loginOtp(req);

        ArgumentCaptor<Otp> otpCaptor = ArgumentCaptor.forClass(Otp.class);

        assertNotNull(otpDto);
        assertEquals("3214", otpDto.getOtp());
        assertEquals(user.getId(), otpDto.getUserId());
    }

    @Test
    public void LoginOtpFailedUserNotFound() {
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("password").build();
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        when(authenticationManager.authenticate(any())).thenReturn(null);
        CustomException exception = assertThrows(CustomException.class, () -> authService.loginOtp(req));

        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "user not found");
    }

    @Test
    public void LoginOtpFailedCacheNotFound() {
        User user = User.builder().id(1L).email("email@email.com").build();
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("password").build();

        when(cacheManager.getCache("otpCache")).thenReturn(null);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        CustomException exception = assertThrows(CustomException.class, () -> authService.loginOtp(req));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 500);
        assertEquals(exception.getMessage(), "server Error");
    }

    @Test
    public void LoginOtpFailedWrongUserNameOrPassword() {
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("password").build();

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("wrong email or password"));

        Exception exception = assertThrows(Exception.class, () -> authService.loginOtp(req));

        assertEquals(exception.getMessage(), "wrong email or password");
    }

    @Test
    public void loginSuccess() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .address("address")
                .password("password")
                .fullname("fullname")
                .dob(new Date(1030820707))
                .build();

        Otp otp = Otp.builder()
                .otp("4512")
                .userId(user.getId())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .tryNumber(5)
                .build();

        RefreshToken refreshToken = RefreshToken.builder().user(user).token("refreshToken").build();

        Cache cache = mock(Cache.class);

        when(refreshService.findByUserId(user.getId())).thenReturn(refreshToken);
        when(cacheManager.getCache("otpCache")).thenReturn(cache);
        when(cache.get(1L, Otp.class)).thenReturn(otp);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("token");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        LoginResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals(res.getFullname(), "fullname");
        assertEquals(res.getToken(), "token");
        assertEquals(res.getRefreshToken(), "refreshToken");
        assertEquals(res.getEmail(), "email@email.com");
        assertEquals(res.getAddress(), "address");
    }

    @Test
    public void loginFailedCacheNotFound() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        when(cacheManager.getCache("otpCache")).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(req));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 500);
        assertEquals(exception.getMessage(), "serverError");
    }

    @Test
    public void loginFailedLoginRequestNotFound() throws Exception {
        Cache cache = mock(Cache.class);
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        when(cacheManager.getCache("otpCache")).thenReturn(cache);
        when(cache.get(1L, Otp.class)).thenReturn(null);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.login(req));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "login request not found");
    }

    @Test
    public void loginFailedOverTry() throws Exception {
        Cache cache = mock(Cache.class);
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();
        Otp otp = Otp.builder().tryNumber(1).otp("1234").expiredAt(LocalDateTime.now().plusMinutes(5)).build();

        when(cacheManager.getCache("otpCache")).thenReturn(cache);
        when(cache.get(1L, Otp.class)).thenReturn(otp);

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(req));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "Too many attempts. Please try again later");
        assertEquals(exception.getErrorCode(), 400);
    }

    @Test
    public void loginSuccessDobInvalid() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("3123").build();
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .address("address")
                .password("password")
                .fullname("fullname")
                .build();
        Otp otp = Otp.builder()
                .otp("3123")
                .userId(user.getId())
                .tryNumber(5)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
        RefreshToken refreshToken = RefreshToken.builder().user(user).token("refreshToken").build();

        Cache cache = mock(Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(1L, Otp.class)).thenReturn(otp);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("token"); 
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(refreshService.createRefreshToken(any(User.class))).thenReturn("refreshToken");

        LoginResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals(res.getFullname(), "fullname");
        assertEquals(res.getToken(), "token");
        assertEquals(res.getRefreshToken(), "refreshToken");
        assertEquals(res.getEmail(), "email@email.com");
        assertEquals(res.getAddress(), "address");
    }

    @Test
    public void loginFailedOtpInvalid() {
        Cache cache = mock(Cache.class);
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();
        Otp otp = Otp.builder()
                .otp("3123")
                .userId(1L)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .tryNumber(5)
                .build();

        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(1L, Otp.class)).thenReturn(otp);

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(req));

        assertEquals(exception.getMessage(), "otp invalid 4 attempts left");
    }

    @Test
    public void loginFailedOtpExpired() {
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .address("address")
                .password("password")
                .fullname("fullname")
                .build();

        Otp otp = Otp.builder()
                .otp("4512")
                .userId(user.getId())
                .expiredAt(LocalDateTime.now().minusMinutes(5))
                .build();
        Cache cache = mock(Cache.class);

        when(cacheManager.getCache("otpCache")).thenReturn(cache);
        when(cache.get(1L, Otp.class)).thenReturn(otp);

        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.login(req));

        assertEquals(exception.getMessage(), "otp expired");
    }

    @Test
    public  void loginFailedUserNotFound() {
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .address("address")
                .password("password")
                .fullname("fullname")
                .build();

        Otp otp = Otp.builder()
                .otp("4512")
                .userId(1L)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();

        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        Cache cache = mock(Cache.class);

        when(cacheManager.getCache("otpCache")).thenReturn(cache);
        when(cache.get(1L, Otp.class)).thenReturn(otp);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> authService.login(req));

        assertEquals(exception.getMessage(), "user not found");
    }

    @Test
    public void forgotPasswordTestSuccess() throws Exception {
        User user = User.builder().id(1L).email("email@email.com").password("password").build();
        String url = environment.getProperty("spring.base-url") + "/auth/reset-password?userId=" + user.getId() +
                "&token=" + mockUUID.toString();
        ForgotPasswordResponse resExpect = ForgotPasswordResponse.builder().url(url).build();
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .token(mockUUID.toString())
                .userId(user.getId())
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
                .build();
        Cache cache = mock(Cache.class);

        when(cacheManager.getCache("forgotPasswordCache")).thenReturn(cache);
        when(cache.get(1L, ForgotPassword.class)).thenReturn(forgotPassword);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);

        ForgotPasswordResponse res = authService.forgotPassword(user.getEmail());

        assertEquals(res.getUrl(), resExpect.getUrl());
        assertEquals(forgotPassword.getToken(), mockUUID.toString());
        assertEquals(forgotPassword.getUserId(), user.getId());
    }

    @Test
    public void forgotPasswordFailedCacheNotFound(){
        User user = User.builder().id(1L).email("email@email.com").password("password").build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cacheManager.getCache("forgotPasswordCache")).thenReturn(null);
        uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);

        CustomException exception = assertThrows(CustomException.class, () -> authService.forgotPassword(anyString()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "server error");
        assertEquals(exception.getErrorCode(), 500);
    }

    @Test
    public void forgotPasswordTestFailedUserNotFound() {
        when(userRepository.findByEmail("email")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, ()->authService.forgotPassword("email"));

        assertEquals(exception.getMessage(), "user not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void resetPasswordSuccess() throws Exception {
        String password = "password";
        String token = "token";
        String id = "1";

        User user = User.builder().id(1L).email("email@email.com").password("password").build();
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .token(token)
                .userId(user.getId())
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
                .build();

        Cache cache = mock(Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(1L, ForgotPassword.class)).thenReturn(forgotPassword);
        when(userRepository.findById(Long.valueOf(id))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(password)).thenReturn(password);

        authService.resetPassword(password, id, token);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User userCapture = captor.getValue();

        assertNotNull(userCapture);
        assertEquals(user.getEmail(), userCapture.getEmail());
        assertEquals(user.getPassword(), userCapture.getPassword());
    }

    @Test
    public void resetPasswordTestFailedCacheNotFound(){
        String password = "password";
        String token = "token";
        String id = "1";
        User user = User.builder().id(1L).email("email@email.com").password("password").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cacheManager.getCache("resetPasswordCache")).thenReturn(null);

        CustomException exception =
                assertThrows(CustomException.class, () -> authService.resetPassword(password, id, token));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "server error");
        assertEquals(exception.getErrorCode(), 500);
    }

    @Test
    public void resetPasswordTestFailedTokenInvalid(){
        String password = "password";
        String token = "token";
        String id = "1";

        User user = User.builder().id(1L).email("email@email.com").password("password").build();
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .token("forgotPasswordToken")
                .userId(user.getId())
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
                .build();

        Cache cache = mock(Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(1L, ForgotPassword.class)).thenReturn(forgotPassword);
        when(userRepository.findById(Long.valueOf(id))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(password)).thenReturn(password);

        CustomException exception =
                assertThrows(CustomException.class, () -> authService.resetPassword(password, id, token));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "token invalid");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void resetPasswordFailedExpiredRequest() throws Exception {
        String password = "password";
        String token = "token";
        String id = "1";

        User user = User.builder().id(1L).email("email@email.com").password("password").build();
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .token(token)
                .userId(user.getId())
                .expiredAt(new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)))
                .build();

        Cache cache = mock(Cache.class);

        when(cacheManager.getCache("forgotPasswordCache")).thenReturn(cache);
        when(cache.get(1L, ForgotPassword.class)).thenReturn(forgotPassword);
        when(userRepository.findById(Long.valueOf(id))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(password)).thenReturn(password);

        CustomException exception = assertThrows(CustomException.class, () -> authService.resetPassword(password, id, token));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "reset password request expired");
    }

    @Test
    public void resetPasswordFailedRequestNotFound() throws Exception {
        String password = "password";
        String token = "token";
        String id = "1";

        User user = User.builder().id(1L).email("email@email.com").password("password").build();

        Cache cache = mock(Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(cache);
        when(cache.get(1L, ForgotPassword.class)).thenReturn(null);
        when(userRepository.findById(Long.valueOf(id))).thenReturn(Optional.of(user));

        CustomException exception = assertThrows(CustomException.class, () -> authService.resetPassword(password, id, token));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "reset password request not found");
    }

    @Test
    public void resetPasswordFailedUserNotFound() throws Exception {
        String password = "password";
        String token = "token";
        String id = "1";

        when(userRepository.findById(Long.valueOf(id))).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> authService.resetPassword(password, id, token));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "user not found");
    }

    @Test
    public void refreshTokenSuccess() throws Exception {
        String token = "token";
        User user = User.builder().id(1L).email("email@email.com").password("password").build();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)))
                .build();

        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(refreshService.findByToken(token)).thenReturn(refreshToken);
        when(jwtUtil.extractUsername(token)).thenReturn("email@email.com");
        when(jwtUtil.generateToken(user)).thenReturn("new token");

        LoginResponse res = authService.refreshToken(token);

        assertNotNull(res);
        assertEquals(res.getToken(), "new token");
    }

    @Test
    public void refreshTokenFailedTokenExpired() throws Exception {
        User user = User.builder().id(1L).email("email@email.com").password("password").build();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiredAt(new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)))
                .token(mockUUID.toString())
                .build();

        when(refreshService.findByToken(anyString())).thenReturn(refreshToken);

        CustomException exception = assertThrows(CustomException.class, () -> authService.refreshToken(anyString()));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 400);
        assertEquals(exception.getMessage(), "refresh token expired");
    }

    @Test
    public void refreshTokenFailedUserNotFound() throws Exception {
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
        when(refreshRepository.findByToken("token")).thenReturn(Optional.empty());
        when(refreshService.findByToken(anyString())).thenThrow(new CustomException(404, "token invalid"));

        CustomException exception = assertThrows(CustomException.class, () -> authService.refreshToken(anyString()));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "token invalid");
    }

    @AfterEach
    public void tearDown() {
        uuidMockedStatic.close();
        mockedStatic.close();
    }
}
