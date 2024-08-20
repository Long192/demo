package com.example.demo.Service;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.example.demo.Exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.Dto.Request.UpdateUserRequest;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

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
        return userRepository.findById(id).orElseThrow(() -> new CustomException(404, "user not found"));
    }

    public Page<User> findAll(Pageable page, String search) {
        return userRepository.findByEmailOrFullname(page, search);
    }

    public void updateUser(UpdateUserRequest req) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userInfo = findById(me.getId());

        if (req.getPassword() != null) {
            userInfo.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if(req.getDob() != null && !req.getDob().isBlank()){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            userInfo.setDob(new Date(sdf.parse(req.getDob()).getTime()));
        }

        if(req.getAvatar() != null && !req.getAvatar().isEmpty()){
            userInfo.setAvatar(imageService.uploadAndGetUrl(req.getAvatar()));
        }

        if(req.getFullname() != null && !req.getFullname().isBlank()){
            userInfo.setFullname(req.getFullname());
        }

        if(req.getEtc() != null && !req.getEtc().isBlank()){
            userInfo.setEtc(req.getEtc());
        }

        if(req.getAddress() != null && !req.getAddress().isBlank()){
            userInfo.setAddress(req.getAddress());
        }
   
        userRepository.save(userInfo);
    }
}
