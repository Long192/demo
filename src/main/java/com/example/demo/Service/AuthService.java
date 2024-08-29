package com.example.demo.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Dto.Request.GetTokenRequest;
import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Dto.Response.ForgotPasswordResponse;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Enum.RoleEnum;
import com.example.demo.Enum.StatusEnum;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.ForgotPassword;
import com.example.demo.Model.Otp;
import com.example.demo.Model.RefreshToken;
import com.example.demo.Model.User;
import com.example.demo.Repository.ForgotPasswordRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Utils.AppUtils;
import com.example.demo.Utils.JwtUtil;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;
    @Autowired
    private RefreshService refreshService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private Environment environment;

    public UserDto signUp(SignUpRequest request) throws Exception {
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEtc(request.getEtc());
        user.setRole(RoleEnum.user);
        user.setFullname(request.getFullname());
        user.setAvatar(request.getAvatar());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        dateFormat.setLenient(false);
        try {
            if (request.getDob() != null && !request.getDob().isBlank()) {
                java.util.Date date = dateFormat.parse(request.getDob());
                if(date.after(new java.util.Date())) {
                    throw new CustomException(400, "Dob invalid");
                }
                user.setDob(new Date(date.getTime()));
            }
        } catch (ParseException  e) {
            throw new CustomException(400, "Dob invalid");
        }
        user.setStatus(StatusEnum.active);
        user.setAddress(request.getAddress());
        User result = userRepository.save(user);
        return mapper.map(result, UserDto.class);
    }

    public LoginResponse login(GetTokenRequest request) throws Exception {
        LoginResponse response = new LoginResponse();
        Otp otp = null;
        Cache cache = cacheManager.getCache("otpCache");
        if(cache == null){
            throw new CustomException(500, "serverError");
        }

        otp = cache.get(request.getUserId(), Otp.class);

        if (otp == null || !validateOtp(otp)) {
            throw new BadCredentialsException("login request not found");
        }

        if(!otp.getOtp().equals(request.getOtp()) ){
            if(otp.getTryNumber() == 1){
                cache.evict(otp.getUserId());
                throw new CustomException(400, "Too many attempts. Please try again later");
            }
            otp.setTryNumber(otp.getTryNumber() - 1);
            cache.put(otp.getUserId(), otp);
            throw new CustomException(400, "otp invalid " + otp.getTryNumber() + " attempts left");
        }

        User user = userRepository.findById(otp.getUserId())
                .orElseThrow(() -> new CustomException(404, "user not found"));
        RefreshToken refreshToken = refreshService.findByUserId(user.getId());
        String jwt = jwtUtil.generateToken(user);
        response.setToken(jwt);
        response.setRefreshToken(
                refreshToken != null ? refreshToken.getToken() : refreshService.createRefreshToken(user)
        );
        response.setId(user.getId());
        response.setAddress(user.getAddress());
        response.setDob(user.getDob() != null ? user.getDob().toString() : null);
        response.setEmail(user.getEmail());
        response.setFullname(user.getFullname());
        cache.evict(user.getId());
        return response;
    }

    public OtpDto loginOtp(LoginRequest request) throws Exception {
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (RuntimeException e) {
            throw new Exception("wrong email or password");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(404, "user not found"));
        OtpDto otpDto = new OtpDto();
        Otp cacheOtp = Otp.builder()
                .userId(user.getId())
                .otp(AppUtils.generateOtp())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .tryNumber(5)
                .build();
        Cache cache = cacheManager.getCache("otpCache");
        if(cache == null){
            throw new CustomException(500, "server Error");
        }
        cache.put(user.getId(), cacheOtp);
        otpDto.setOtp(cacheOtp.getOtp());
        otpDto.setUserId(cacheOtp.getUserId());
        return otpDto;
    }

    public ForgotPasswordResponse forgotPassword(String email) throws Exception {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new CustomException(404, "user not found"));
        ForgotPasswordResponse urlResponse = new ForgotPasswordResponse();
        String token = UUID.randomUUID().toString();
        String url =
                environment.getProperty("base-url") + "/auth/reset-password?userId=" + user.getId() + "&token=" + token;
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setUser(user);
        forgotPassword.setToken(token);
        forgotPassword.setExpiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
        forgotPasswordRepository.save(forgotPassword);
        urlResponse.setUrl(url);
        return urlResponse;
    }

    public void resetPassword(String password, String id, String token) throws Exception {
        User user =
                userRepository.findById(Long.valueOf(id)).orElseThrow(() -> new CustomException(404, "user not found"));
        ForgotPassword forgotPassword = forgotPasswordRepository.findByUserIdAndToken(Long.valueOf(id), token);
        if (forgotPassword == null)
            throw new CustomException(404, "reset password request not found");
        if (validateResetPasswordToken(forgotPassword, token))
            throw new CustomException(404, "reset password request expired");
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public LoginResponse refreshToken(String refreshToken) throws Exception {
        LoginResponse response = new LoginResponse();
        RefreshToken refresh = refreshService.findByToken(refreshToken);
        User user = refresh.getUser();
        String token = jwtUtil.generateToken(user);
        response.setToken(token);
        return response;
    }

    private boolean validateOtp(Otp otp) {
        return otp.getExpiredAt().isAfter(LocalDateTime.now());
    }

    private boolean validateResetPasswordToken(ForgotPassword forgotPassword, String token) {
        return forgotPassword.getExpiredAt().before(new Timestamp(System.currentTimeMillis()));
    }
}
