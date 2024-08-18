package com.example.demo.Service;

import com.example.demo.Dto.Request.UpdateUserRequest;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User findById(Long id) throws Exception {
        return userRepository.findById(id).orElseThrow(() -> new Exception("user not found"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void updateUser(UpdateUserRequest req) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userInfo = findById(me.getId());

        if (req.getPassword() != null) {
            userInfo.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if(req.getDob() != null && !req.getDob().isEmpty()){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            userInfo.setDob(new Date(sdf.parse(req.getDob()).getTime()));
        }

        if(req.getAvatar() instanceof MultipartFile){
            userInfo.setAvatar(imageService.uploadAndGetUrl((MultipartFile) req.getAvatar()));
        }else if(req.getAvatar() instanceof String){
            userInfo.setAvatar(req.getAvatar().toString())  ;
        }

        userInfo.setFullname(req.getFullname());
        userInfo.setEtc(req.getEtc());
        userInfo.setAddress(req.getAddress());
        userRepository.save(userInfo);
    }
}
