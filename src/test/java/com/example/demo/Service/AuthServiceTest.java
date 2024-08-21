package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.Dto.Request.GetTokenRequest;
import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Otp;
import com.example.demo.Model.User;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Utils.AppUtils;
import com.example.demo.Utils.JwtUtil;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageService imageService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private JwtUtil jwtUtil;

    MockedStatic<AppUtils> mockedStatic;

    @BeforeEach
    public void setup() {
        mockedStatic = mockStatic(AppUtils.class);
    }

    @Test
    public void SignupTestSuccess() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE, "avatar".getBytes());
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        SignUpRequest req = SignUpRequest.builder().email("email@testemail.com").password("password").etc("etc")
                .address("address").fullname("fullname").dob("2002/01/09").build();
        User user = User.builder().email(req.getEmail()).password(req.getPassword()).etc(req.getEtc())
                .address(req.getAddress()).fullname(req.getFullname())
                .dob(new Date(format.parse(req.getDob()).getTime())).build();

        when(passwordEncoder.encode(req.getPassword())).thenReturn("password");
        when(imageService.uploadAndGetUrl(file)).thenReturn("url");

        authService.signUp(req);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User captureUser = userCaptor.getValue();

        assertEquals(user.getEmail(), captureUser.getEmail());
        assertEquals(user.getAddress(), captureUser.getAddress());
        assertEquals(user.getDob(), captureUser.getDob());
        assertEquals(user.getEtc(), captureUser.getEtc());
        assertEquals(user.getFullname(), captureUser.getFullname());
        assertEquals(user.getPassword(), captureUser.getPassword());
    }

    @Test
    public void LoginOtpSuccess() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("password").build();
        User user = User.builder().id(1L).email("email@email.com").build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        mockedStatic.when(() -> AppUtils.generateOtp()).thenReturn("3214");

        OtpDto otpDto = authService.loginOtp(req);

        verify(otpRepository, atLeastOnce()).save(any(Otp.class));

        assertNotNull(otpDto);
        assertEquals("3214", otpDto.getOtp());
        assertEquals(user.getId(), otpDto.getUserId());
    }

    @Test
    public void LoginOtpFailedUserNotFound() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("password").build();
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        when(authenticationManager.authenticate(any())).thenReturn(null);
        CustomException exception = assertThrows(CustomException.class, () -> authService.loginOtp(req));

        verify(otpRepository, never()).save(any());

        assertEquals(exception.getErrorCode(), 404);
        assertEquals(exception.getMessage(), "user not found");
    }

    @Test
    public void LoginOtpFailedWrongUserNameOrPassword() throws Exception {
        LoginRequest req = LoginRequest.builder().email("email@email.com").password("password").build();

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("wrong email or password"));

        Exception exception = assertThrows(Exception.class, () -> authService.loginOtp(req));

        verify(otpRepository, never()).save(any());

        assertEquals(exception.getMessage(), "wrong email or password");
    }

    @Test
    public void loginSuccess() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        User user = User.builder()
                .email("email@email.com")
                .address("address")
                .password("password")
                .fullname("fullname")
                .build();

        Otp otp = Otp.builder()
                .otp("3123")
                .user(user)
                .expiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)))
                .build();
                

        when(otpRepository.findTopByOtpAndUserIdOrderByUserIdDesc(req.getOtp(), req.getUserId())).thenReturn(otp);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("token");
        when(jwtUtil.generateRefreshToken(new HashMap<>(), user)).thenReturn("refresh");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        LoginResponse res = authService.login(req);

        assertNotNull(res);
        assertEquals(res.getFullname(), "fullname");
        assertEquals(res.getToken(), "token");
        assertEquals(res.getRefreshToken(), "refresh");
        assertEquals(res.getEmail(), "email@email.com");
        assertEquals(res.getAddress(), "address");
    }

    @Test
    public void loginFailedOtpInvalid() throws Exception {
        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        when(otpRepository.findTopByOtpAndUserIdOrderByUserIdDesc(req.getOtp(), req.getUserId())).thenReturn(null);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.login(req));

        assertEquals(exception.getMessage(), "otp or user invalid");
    }

    @Test
    public void loginFailedOtpExpired() {
        User user = User.builder()
        .email("email@email.com")
        .address("address")
        .password("password")
        .fullname("fullname")
        .build();

        Otp otp = Otp.builder()
                .otp("4512")
                .user(user)
                .expiredAt(new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)))
                .build();

        GetTokenRequest req = GetTokenRequest.builder().UserId(1L).otp("4512").build();

        when(otpRepository.findTopByOtpAndUserIdOrderByUserIdDesc(req.getOtp(), req.getUserId())).thenReturn(otp);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> authService.login(req));

        assertEquals(exception.getMessage(), "otp or user invalid");
    }

    

    @AfterEach
    public void tearDown() {
        mockedStatic.close();
    }
}
