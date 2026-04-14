package com.campus.trade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // 1. 获取最纯净的绝对路径（消除路径中的 ".\"）
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String absolutePath = dir.getCanonicalPath() + File.separator;

            // 2. 强制将 Windows 的反斜杠 \ 替换为正斜杠 / (Spring 核心要求)
            absolutePath = absolutePath.replace("\\", "/");

            // 打印出来方便你在控制台核对
            System.out.println("=====================================");
            System.out.println("👉 图片物理仓库: " + absolutePath);
            System.out.println("=====================================");

            // 3. 💡 双保险映射：同时兼容带 /api 和不带 /api 的请求
            registry.addResourceHandler("/uploads/**", "/api/uploads/**")
                    .addResourceLocations("file:" + absolutePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}