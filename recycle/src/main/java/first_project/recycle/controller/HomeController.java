package first_project.recycle.controller;

import first_project.recycle.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final BoardService boardService;

    // 브라우저에서 "/" 경로로 접근했을 때 실행
    // 메인페이지에 최신 게시글 전달
    @GetMapping("/")
//    public String home() {
//        return "index"; // templates/index.html 파일을 열어줌 (파일명이 main.html이면 "main"으로 변경)
    public String home(Model model) {
        model.addAttribute(
                "recentBoards",
                boardService.getRecentBoards()
        );
        return "index";
    }
}
