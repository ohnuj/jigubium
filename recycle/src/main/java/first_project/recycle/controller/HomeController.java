package first_project.recycle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 브라우저에서 "/" 경로로 접근했을 때 실행
    @GetMapping("/")
    public String home() {
        return "index"; // templates/index.html 파일을 열어줌 (파일명이 main.html이면 "main"으로 변경)
    }
}