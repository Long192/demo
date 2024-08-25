package com.example.demo.Service;

import com.example.demo.Dto.Request.UpdateUserRequest;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Image;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {
    User user = User.builder().id(1L).fullname("fullname").email("email").build();

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ImageService imageService;
    @Mock
    private UploadService uploadService;

    @BeforeEach
    public void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void loadUserByUsernameSuccess() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        User res = (User) userService.loadUserByUsername(user.getEmail());

        assertNotNull(res);
        assertEquals(res, user);

    }

    @Test
    public void loadUserByUsernameFailed() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> userService.loadUserByUsername(user.getEmail()));

        assertNotNull(exception);
    }

    @Test
    public void findByIdSuccess() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User res = userService.findById(1L);

        assertNotNull(res);
        assertEquals(res, user);
    }

    @Test
    public void findByIdFailedUserNotFound() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> userService.findById(1L));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "user not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void getAllUser() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest req = PageRequest.of(0, 10, sort);

        when(userRepository.findByEmailOrFullname(req, "" )).thenReturn(new PageImpl<>(List.of(user)));

        Page<User> users = userService.findAll(req, "");

        assertNotNull(users.getContent());
        assertEquals(users.getContent().getFirst(), user);
    }

    @Test
    public void getAllUserFailedWrongSortBy() throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "wrong property");
        PageRequest req = PageRequest.of(0, 10, sort);

        when(userRepository.findByEmailOrFullname(req, "" ))
                .thenThrow(new InvalidDataAccessApiUsageException("wrong sort by"));

        Exception exception = assertThrows(Exception.class, () -> userService.findAll(req, ""));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "wrong sort by");
    }

    @Test
    public void updateUserSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("avatar", "avatar.jpeg", MediaType.IMAGE_JPEG_VALUE,
                "avatar".getBytes());
        UpdateUserRequest req = UpdateUserRequest.builder()
                .etc("etc")
                .fullname("new full name")
                .password("new password")
                .address("address")
                .dob("2002/09/01")
                .avatar(file)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.updateUser(req);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User userCaptured = userCaptor.getValue();

        assertNotNull(userCaptured);
        assertEquals(userCaptured, user);
    }

    @Test
    public void updateUserSuccessAllParamNull () throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.updateUser(req);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User userCaptured = userCaptor.getValue();

        assertNotNull(userCaptured);
        assertEquals(userCaptured, user);
    }

    @Test
    public void updateUserSuccessAllParamBlank() throws Exception {
        UpdateUserRequest req = UpdateUserRequest.builder()
                .etc("")
                .fullname("")
                .password("")
                .address("")
                .dob("")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.updateUser(req);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User userCaptured = userCaptor.getValue();

        assertNotNull(userCaptured);
        assertEquals(userCaptured, user);
    }

}