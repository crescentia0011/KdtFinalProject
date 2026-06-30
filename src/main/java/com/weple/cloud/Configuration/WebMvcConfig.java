package com.weple.cloud.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드 파일은 /uploads/** 로 분리
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/weple_uploads/tasks/");
    }
}