package com.example.demo.Service;

import java.net.MalformedURLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Enum.RoleEnum;
import com.example.Enum.StatusEnum;
import com.example.demo.Dto.Request.GetTokenRequest;
import com.example.demo.Dto.Request.LoginRequest;
import com.example.demo.Dto.Request.SignUpRequest;
import com.example.demo.Dto.Response.ForgotPasswordResponse;
import com.example.demo.Dto.Response.LoginResponse;
import com.example.demo.Dto.Response.OtpDto;
import com.example.demo.Model.ForgotPassword;
import com.example.demo.Model.Otp;
import com.example.demo.Model.User;
import com.example.demo.Repository.ForgotPasswordRepository;
import com.example.demo.Repository.OtpRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Utils.AppUtils;
import com.example.demo.Utils.JwtUtil;
import com.uploadcare.upload.UploadFailureException;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private ForgotPasswordRepository forgotPasswordRepository;
    @Autowired
    private ImageService imageService;
    @Autowired
    private Environment environment;

    public void signUp(SignUpRequest request) throws MalformedURLException, UploadFailureException, ParseException {
        User user = new User();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEtc(request.getEtc());
        user.setRole(RoleEnum.user);
        user.setAvatar(request.getAvatar() != null ? imageService.uploadAndGetUrl(request.getAvatar()) : null);
        user.setFullname(request.getFullname());
        user.setDob(request.getDob() != null ? new Date(dateFormat.parse(request.getDob()).getTime()) : null);
        user.setStatus(StatusEnum.active);
        user.setAddress(request.getAddress());
        userRepository.save(user);
    }

    public LoginResponse login(GetTokenRequest request) {
        LoginResponse response = new LoginResponse();
        Otp otp = otpRepository.findTopByOtpAndUserIdOrderByUserIdDesc(request.getOtp(), Long.valueOf(request.getUserId()));
        if (otp != null && validateOtp(otp)) {
            User user = userRepository.findById(otp.getUser().getId()).orElseThrow();
            String jwt = jwtUtil.generateTokken(user);
            response.setToken(jwt);
            response.setId(user.getId());
            response.setAddress(user.getAddress());
            response.setDob(user.getDob() != null ? user.getDob().toString() : null);
            response.setEmail(user.getEmail());
            response.setFullname(user.getFullname());
            otpRepository.deleteById(otp.getId());
        } else {
            throw new BadCredentialsException("otp or user invalid");
        }
        return response;
    }

    public OtpDto loginOtp(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        OtpDto otpDto = new OtpDto();
        if (user.isPresent()) {
            Otp otp = new Otp();
            otp.setOtp(AppUtils.generateOtp());
            otp.setExpiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
            otp.setUser(user.get());
            otpRepository.save(otp);
            otpDto.setOtp(otp.getOtp());
            otpDto.setUserId(otp.getUser().getId());
        }
        return otpDto;
    }

    public ForgotPasswordResponse forgotPassword(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        ForgotPasswordResponse urlResponse = new ForgotPasswordResponse();
        if (user.isPresent() && user.get().getId() != null) {
            String token = UUID.randomUUID().toString();
            String url = environment.getProperty("spring.base-url") + "/auth/reset-password?userId=" + user.get().getId() + "&token=" + token;
            ForgotPassword forgotPassword = new ForgotPassword();
            forgotPassword.setUser(user.get());
            forgotPassword.setToken(token);
            forgotPassword.setExpiredAt(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
            forgotPasswordRepository.save(forgotPassword);
            urlResponse.setUrl(url);
        }
        return urlResponse;
    }

    public void resetPassword(String password, String id, String token) {
        Optional<User> user = userRepository.findById(Long.valueOf(id));
        ForgotPassword forgotPassword = forgotPasswordRepository.findByUserIdAndToken(Long.valueOf(id), token);
        if (user.isPresent() && forgotPassword != null && validateResetPasswordToken(forgotPassword, token)) {
            user.get().setPassword(passwordEncoder.encode(password));
            userRepository.save(user.get());
        }
    }

    private boolean validateOtp(Otp otp) {
        return otp.getOtp() != null && !otp.getExpiredAt().before(new Timestamp(System.currentTimeMillis()));
    }

    private boolean validateResetPasswordToken(ForgotPassword forgotPassword, String token) {
        return !forgotPassword.getExpiredAt().before(new Timestamp(System.currentTimeMillis())) || !forgotPassword.getToken().equals(token);
    }
}
