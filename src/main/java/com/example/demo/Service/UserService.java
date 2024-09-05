package com.example.demo.Service;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.Dto.Request.UpdateUserRequest;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public User findById(Long id) throws Exception {
        return userRepository.findById(id).orElseThrow(() -> new CustomException(404, "user not found"));
    }

    public Page<User> findAll(Pageable page, String search) throws Exception {
        try{
            return userRepository.findByEmailOrFullname(page, search);
        }catch(InvalidDataAccessApiUsageException e){
            throw new Exception("wrong sort by");
        }
    }

    @Transactional
    public void save(User user){
        userRepository.save(user);
    }

    public UserDto updateUser(UpdateUserRequest req) throws Exception {
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userInfo = findById(me.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        if(req.getDob() != null && !req.getDob().isBlank()){
            try{
                userInfo.setDob(new Date(sdf.parse(req.getDob()).getTime()));
            }catch(ParseException e){
                throw new CustomException(400, "Dob invalid");
            }
        }

        if(req.getAvatar() != null){
            userInfo.setAvatar(req.getAvatar());
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
   
        User user = userRepository.save(userInfo);
        return mapper.map(user, UserDto.class);
    }
}
