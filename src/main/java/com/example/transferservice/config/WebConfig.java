package com.example.transferservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 모든 컨트롤러 경로에 /api를 자동으로 붙임
        configurer.addPathPrefix("/api", c -> true);
    }
}