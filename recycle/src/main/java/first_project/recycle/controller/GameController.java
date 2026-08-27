package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.SessionUser;
import first_project.recycle.service.GameService.GameService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/game")
    public String gamePage(HttpSession session, Model model, HttpServletResponse response) throws IOException {
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);

        Long memberId = loginUser.getMemberId();

        // 오늘 플레이 횟수 확인 (3회 초과 시 템플릿 렌더링 대신 JS alert 후 메인 이동)
        int todayPlayCount = gameService.getTodayPlayCount(memberId);
        if (todayPlayCount >= 3) {
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('오늘 플레이 가능한 횟수(3회)를 모두 소모했습니다.'); location.href='/';</script>");
            out.flush();
            return null;
        }

        List<Map<String, String>> items = gameService.getRandomItems(3);
        model.addAttribute("itemList", items);
        model.addAttribute("remainCount", 3 - todayPlayCount);
        return "game";
    }

    @ResponseBody
    @PostMapping("/game/result")
    public ResponseEntity<Map<String, Object>> saveResult(@RequestBody Map<String, Object> resultData,
                                                          HttpSession session) {
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        Long memberId = loginUser.getMemberId();
        resultData.put("memberId", memberId);
        int earnedPoint = gameService.saveGameResult(resultData);

        return ResponseEntity.ok(Map.of("success", true, "earnedPoint", earnedPoint));
    }
}