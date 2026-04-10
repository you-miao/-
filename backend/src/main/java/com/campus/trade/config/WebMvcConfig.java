package com.campus.trade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    private String resolvedUploadPath;

    @PostConstruct
    public void init() {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        resolvedUploadPath = dir.getAbsolutePath().replace("\\", "/");
        if (!resolvedUploadPath.endsWith("/")) {
            resolvedUploadPath += "/";
        }
        System.out.println("[Upload] 文件存储路径: " + resolvedUploadPath);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:///" + resolvedUploadPath;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
