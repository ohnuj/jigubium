package first_project.recycle.controller;

import first_project.recycle.domain.User;
import first_project.recycle.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 스프링 MVC 컨트롤러로 등록하여 웹 요청/응답 처리
@Controller
// final 필드(userService)에 대한 생성자를 자동 생성하여 의존성 주입(DI)
@RequiredArgsConstructor
public class SignupController {

    // 회원가입 비즈니스 로직 처리를 위한 서비스 주입
    private final UserService userService;

    // GET /signup 요청 처리: 회원가입 폼 화면 반환
    @GetMapping("/signup")
    public String signupForm(Model model) {
        // 폼 바인딩을 위한 빈 User 객체를 생성하여 모델에 전달
        model.addAttribute("user", new User());
        // templates/signupForm.html 렌더링
        return "signupForm";
    }

    // POST /signup 요청 처리: 회원가입 데이터 수신 및 가입 처리
    @PostMapping("/signup")
    public String signup(@ModelAttribute User user,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // 서비스 계층에 가입 로직 위임 (이메일 중복 체크 및 DB 저장)
        boolean isSuccess = userService.signup(user);

        // 중복 이메일 등으로 회원가입 실패 시
        if (!isSuccess) {
            // 에러 메시지를 모델에 담아 회원가입 폼으로 다시 이동 (입력 폼 유지)
            model.addAttribute("error", "이미 사용 중인 이메일입니다.");
            return "signupForm";
        }
        // 회원가입 성공 시 1회성 플래시 속성(FlashAttribute)으로 성공 메시지 전달 (URL에 노출 X)
        // PRG(Post-Redirect-Get) 패턴 적용: 새로고침 시 중복 가입 방지 및 로그인 페이지로 이동
        redirectAttributes.addFlashAttribute("message", "회원가입을 축하합니다! 신규 가입 에코포인트 100P가 지급되었습니다.");
        return "redirect:/login";
    }
}