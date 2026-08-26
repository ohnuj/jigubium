package first_project.recycle.config;


import first_project.recycle.interceptor.AdminInterceptor;
import first_project.recycle.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 업로드 이미지 URL과 실제 저장 폴더를 연결하는 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final AdminInterceptor adminInterceptor;
    private final LoginInterceptor loginInterceptor;

    public WebConfig(
            @Value("${recycle.upload.dir}") String uploadDir,
            AdminInterceptor adminInterceptor,
            LoginInterceptor loginInterceptor) {
        this.uploadDir = uploadDir;
        this.adminInterceptor = adminInterceptor;
        this.loginInterceptor = loginInterceptor;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        // 1. 로그인 여부 검사 > 로그인 해야 사용 가능
        registry.addInterceptor(loginInterceptor).order(1).addPathPatterns(
                "/mypage/**",

                "/boards/write",
                "/boards/*/edit",
                "/boards/*/delete",
                "/boards/*/images/*/delete",
                "/boards/*/like",

                "/boards/*/comments",
                "/boards/*/comments/**",

                "/game",

                "/reward",
                "/reward/**",

                // 공공데이터 DB적재
                "/api/*/import",

                "/admin",
                "/admin/**");
        // 2. 관리자 권한 검사 > 관리자만 사용 가능
        registry.addInterceptor(adminInterceptor).order(2).addPathPatterns(
                "/admin",
                "/admin/**",

                // 관리자만 공공데이터 DB적재
                "/api/*/import");
    }

}
