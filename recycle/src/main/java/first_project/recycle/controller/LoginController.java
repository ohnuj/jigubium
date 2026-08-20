package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.User;
import first_project.recycle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 스프링 MVC 컨트롤러로 등록하여 로그인/로그아웃 관련 HTTP 요청 처리
@Controller
// final 필드(userService)를 초기화하는 생성자를 자동 생성하여 의존성 주입(DI)
@RequiredArgsConstructor
public class LoginController {

    // 로그인 검증 비즈니스 로직을 처리하는 서비스 주입
    private final UserService userService;

    // GET /login 요청 처리: 로그인 화면 반환
    // @RequestParam(defaultValue = "/"): 로그인 성공 후 복귀할 대상 URL 파라미터 수신 (기본값은 홈 "/")
    @GetMapping("/login")
    public String loginForm(@RequestParam(defaultValue = "/") String redirectURL, Model model) {
        // 이전 접근 페이지 URL을 뷰 템플릿(히든 태그 등)으로 전달
        model.addAttribute("redirectURL", redirectURL);
        // templates/loginForm.html 렌더링
        return "loginForm";
    }

    // POST /login 요청 처리: 이메일/비밀번호 데이터 검증 및 세션 생성
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "/") String redirectURL,
                        HttpServletRequest request,
                        Model model) {

        // 입력받은 이메일/비밀번호로 회원 검증 수행
        User loginUser = userService.login(email, password);

        // 검증 실패(회원 미존재 또는 비밀번호 불일치) 시
        if (loginUser == null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 맞지 않습니다.");
            model.addAttribute("email", email); // 사용자가 입력했던 이메일 유지
            model.addAttribute("redirectURL", redirectURL); // 리다이렉트 URL 유지
            return "loginForm";
        }

        // 로그인 성공: 현재 요청의 세션을 가져오거나 없으면 신규 생성
        HttpSession session = request.getSession();

        // 세션 내 비밀번호 노출 방지를 위한 민감 정보 제거 (보안 조치)
        loginUser.setPassword(null);

        // 로그인 여부 확인 플래그 저장
        session.setAttribute("checkLogin", true);

        // 상수 키(SessionConst.LOGIN_MEMBER)를 사용하여 세션에 회원 객체 바인딩
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginUser);

        // 로그인 전 접근 시도했던 URL 또는 기본 URL로 이동
        return "redirect:" + redirectURL;
    }

    // Get /logout 요청 처리: 세션 무효화 및 로그아웃
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // 세션이 존재하면 가져오고 없으면 새로 만들지 않음 (false 옵션)
        HttpSession session = request.getSession(false);

        // 존재하는 세션이 있다면 세션 내부의 모든 데이터 삭제 및 만료 처리
        if (session != null) {
            session.invalidate();
        }

        // 로그아웃 후 로그인 화면으로 리다이렉트
        return "redirect:/login";
    }
}