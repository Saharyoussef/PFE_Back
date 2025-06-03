package com.sahar.dashboardservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/dashboard/screenshots_data/**")
                .addResourceLocations("file:///C:/Users/Sahar Y/OneDrive/Bureau/PFE/data_capture_selenium/screenshots_data/");
    }
}
// This configuration class maps the URL path "/screenshots_data/**" to the local file system directory