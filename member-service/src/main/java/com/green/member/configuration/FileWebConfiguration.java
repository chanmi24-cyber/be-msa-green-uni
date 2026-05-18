package com.green.member.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FileWebConfiguration implements WebMvcConfigurer {

    private final String fileUploadPath;

    public FileWebConfiguration(@Value("${constants.file.directory}") String fileUploadPath) {
        this.fileUploadPath = fileUploadPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + fileUploadPath + "/");
    }
}
