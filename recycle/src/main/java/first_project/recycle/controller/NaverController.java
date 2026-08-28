package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.SessionUser;
import first_project.recycle.domain.User;
import first_project.recycle.service.NaverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.Base64;

@Controller
@RequiredArgsConstructor
public class NaverController {

    private final NaverService naverService;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    /**
     * 네이버 로그인 시작
     */
    @GetMapping("/oauth/naver")
    public String naverLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);

        // state 난수 생성 및 세션 저장 (CSRF 방어)
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        session.setAttribute("NAVER_OAUTH_STATE", state);

        String authUrl = "https://nid.naver.com/oauth2.0/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;

        return "redirect:" + authUrl;
    }

    /**
     * 네이버 로그인 Callback
     */
    @GetMapping("/oauth/naver/callback")
    public String naverCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String error,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            redirectAttributes.addFlashAttribute("error", "네이버 로그인 세션이 만료되었습니다.");
            return "redirect:/login";
        }

        String savedState = (String) session.getAttribute("NAVER_OAUTH_STATE");
        session.removeAttribute("NAVER_OAUTH_STATE");

        // state 검증
        if (state == null || savedState == null || !savedState.equals(state)) {
            redirectAttributes.addFlashAttribute("error", "네이버 로그인 인증에 실패했습니다.");
            return "redirect:/login";
        }

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", "네이버 로그인이 취소되었거나 인증에 실패했습니다.");
            return "redirect:/login";
        }

        if (code == null || code.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "네이버 로그인 인증 코드가 없습니다.");
            return "redirect:/login";
        }

        // 토큰 발급 및 회원 로그인/회원가입 처리
        String accessToken = naverService.getAccessToken(code, state);
        User loginUser = naverService.naverLoginProcess(accessToken);

        if (loginUser.isNewUser()) {
            redirectAttributes.addFlashAttribute("message", "회원가입을 축하합니다. 신규 가입 에코포인트 100p가 지급되었습니다.");
        }


        // 세션 고정 보호 및 로그인 세션 저장
        request.changeSessionId();

        SessionUser sessionUser = new SessionUser(
                loginUser.getMemberId(),
                loginUser.getNickname(),
                loginUser.getProvider(),
                loginUser.getRole()
        );
        session.setAttribute("checkLogin", true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, sessionUser);

        return "redirect:/";
    }
}