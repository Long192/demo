package com.example.demo.Config;

import com.uploadcare.api.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class UploadCareConfig {
    @Autowired
    private Environment env;

    @Bean
    public Client uploadClient() {
        return new Client(env.getProperty("uploadcare-public"), env.getProperty("uploadcare-secret"));
    }
}
