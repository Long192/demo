package com.example.demo.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.uploadcare.api.Client;

@Configuration
public class UploadCareConfig {
    @Autowired
    private Environment env;

    @Bean
    public Client uploadClient(){
        return new Client(env.getProperty("uploadcare-public"), env.getProperty("uploadcare-secret"));
    }
}
