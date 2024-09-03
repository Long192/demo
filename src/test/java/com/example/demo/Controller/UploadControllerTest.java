package com.example.demo.Controller;

import com.example.demo.Service.UploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UploadControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UploadService uploadService;

    @Test
    @WithMockUser
    public void testUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                "Hello World".getBytes());

        when(uploadService.uploadAndGetUrl(file)).thenReturn("url");

        mockMvc.perform(multipart(HttpMethod.POST, "/upload")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("success"))
                .andExpect(jsonPath("status").value(200))
                .andExpect(jsonPath("data.url").value("url"));
    }
}