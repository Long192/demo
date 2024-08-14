package com.example.demo.Config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
    @Value("${base-url}")
    private String url;

    @Bean
    public OpenAPI swaggerSetup() {
        Server devServer = new Server();
        devServer.setUrl(url);
        devServer.setDescription("Server URL in delvelopment enviroment");

        Info info = new Info();
        info.title("Tutorial Management API").version("1.0")
                .description("This API exposes endpoints to manage tutorials");
        return new OpenAPI().info(info).servers(List.of(devServer))
            .addSecurityItem( new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKScheme()));
    }

    private SecurityScheme createAPIKScheme(){
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer");
    }
}
