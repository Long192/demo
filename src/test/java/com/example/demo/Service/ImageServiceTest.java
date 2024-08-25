package com.example.demo.Service;

import com.example.demo.Model.Image;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.ImageRepository;
import com.uploadcare.api.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ImageServiceTest {
    MockMultipartFile file =
            new MockMultipartFile("avatar", "avatar.jpeg", "image/jpeg", "image/jpeg".getBytes());
    Post post = Post.builder()
            .id(1L)
            .content("content")
            .images(new ArrayList<>())
            .build();

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UploadService uploadService;

    @Mock
    private Client client;

    @BeforeEach
    public void setUp(){
        User user = User.builder().id(1L).email("email").fullname("fullname").build();

        Authentication authentication =new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @Transactional
    public void uploadSuccess() throws Exception {
        when(uploadService.uploadAndGetUrl(any(MultipartFile.class))).thenReturn("url");

        List<Image> images = imageService.upload(List.of(file), post);

        ArgumentCaptor<List<Image>> imageCaptor = ArgumentCaptor.forClass(List.class);

        verify(imageRepository, times(1)).saveAll(imageCaptor.capture());

        List<Image> imagesCaptured = imageCaptor.getValue();

        assertNotNull(images);
        assertEquals(images, imagesCaptured);
    }

    @Test
    public void editImageSuccessAddNew() throws Exception {
        when(uploadService.uploadAndGetUrl(any(MultipartFile.class))).thenReturn("url");

        List<Image> images = imageService.editImage(List.of(file), post, null);

        ArgumentCaptor<List<Image>> imageCaptor = ArgumentCaptor.forClass(List.class);

        verify(imageRepository, times(1)).saveAll(imageCaptor.capture());

        List<Image> imagesCaptured = imageCaptor.getValue();

        assertNotNull(images);
        assertEquals(images, imagesCaptured);
    }
    @Test
    public void editImageSuccessRemove() throws Exception {
        when(uploadService.uploadAndGetUrl(any(MultipartFile.class))).thenReturn("url");

        List<Image> images = imageService.editImage(null, post, List.of("url"));

        ArgumentCaptor<List<Image>> imageCaptor = ArgumentCaptor.forClass(List.class);

        verify(imageRepository, times(1)).deleteAll(imageCaptor.capture());

        List<Image> imagesCaptured = imageCaptor.getValue();

        assertNotNull(images);
        assertEquals(images, imagesCaptured);
    }


}