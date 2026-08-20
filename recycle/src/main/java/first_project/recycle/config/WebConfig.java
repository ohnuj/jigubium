package first_project.recycle.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 업로드 이미지 URL과 실제 저장 폴더를 연결하는 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(
            @Value("${recycle.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        String location =
                Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .toUri()
                        .toString();

        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }

}
