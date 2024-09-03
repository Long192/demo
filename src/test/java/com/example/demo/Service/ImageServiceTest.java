package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

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

import com.example.demo.Model.Image;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.ImageRepository;
import com.uploadcare.api.Client;

@SpringBootTest
public class ImageServiceTest {
    Post post = Post.builder()
            .id(1L)
            .content("content")
            .images(new ArrayList<>())
            .build();

    @InjectMocks
    private ImageService imageService;
    @Mock
    private ImageRepository imageRepository;


    @BeforeEach
    public void setUp(){
        User user = User.builder().id(1L).email("email").fullname("fullname").build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @Transactional
    public void uploadSuccess() throws Exception {
        Image image = Image.builder()
                .id(1L)
                .post(post)
                .url("url")
                .build();
        List<Image> images = List.of(image);

        when(imageRepository.saveAll(any())).thenReturn(images);

        List<Image> res = imageService.saveImages(List.of("url"), post);

        assertNotNull(res);
        assertEquals(res, images);
    }

    @Test
    public void saveImageNullUrls() {
        List<Image> images = imageService.saveImages(null, post);

        assertNull(images);
    }

    @Test
    public void saveImageEmptyUrls() {
        List<Image> images = imageService.saveImages(List.of(), post);

        assertNull(images);
    }



    @Test
    public void editImageSuccessAddNew() throws Exception {
        List<Image> images = imageService.editImage(List.of("url1"), post);

        assertNotNull(images);
        assertEquals(images.getFirst().getUrl(), "url1");
    }


}