package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.SessionUser;
import first_project.recycle.domain.User;
import first_project.recycle.service.GoogleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Controller
// final 필드(googleService) 생성자 주입
@RequiredArgsConstructor
public class GoogleController {

    // 구글 OAuth API 통신 및 DB 처리를 담당하는 서비스 객체 의존성 주입
    private final GoogleService googleService;

    // application.properties에 설정된 구글 클라이언트 ID 주입
    @Value("${google.client-id}")
    private String clientId;

    // application.properties에 설정된 인가 코드 수신용 콜백 URL 주입
    @Value("${google.redirect-uri}")
    private String redirectUri;

    /**
     * 구글 로그인 시작 엔드포인트
     * 브라우저를 구글 인증 서버의 로그인/동의 요청 페이지로 리다이렉트
     */
    @GetMapping("/oauth/google")
    public String googleLogin(HttpServletRequest request) {

        HttpSession session = request.getSession(true);

        // OAuth state 생성
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // callBack 후 비교하기 위해 세션에 저장
        session.setAttribute("GOOGLE_OAUTH_STATE", state);

        // 구글 인가 코드 요청을 위한 scope 인코딩 (openid, email, profile)
        String scope = URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);

        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=" + scope
                + "&state=" + state
                + "&prompt=select_account";

        // 조합된 구글 인증 URL로 사용자 브라우저 강제 이동
        return "redirect:" + authUrl;
    }

    /**
     * 구글 로그인 콜백 엔드포인트
     * 사용자가 구글 로그인을 완료했을 때 구글 인증 서버가 인가 코드(code)를 담아 호출
     */
    @GetMapping("/oauth/google/callback")
    public String googleCallback(@RequestParam(required = false) String code,
                                 @RequestParam(required = false) String state,
                                 @RequestParam(required = false) String error,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);

        // 1. 세션 검증
        if (session == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "구글 로그인에 실패했습니다.");
            return "redirect:/login";
        }

        // 2. state 검증
        String savedState = (String) session.getAttribute("GOOGLE_OAUTH_STATE");

        // state 일회용 폐기
        session.removeAttribute("GOOGLE_OAUTH_STATE");

        // 3. state 검증
        if (state == null || savedState == null || !savedState.equals(state)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "구글 로그인 인증에 실패했습니다.");
            return "redirect:/login";
        }

        // 3. 에러 파라미터 체크
        if (error != null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "구글 로그인이 취소되었거나 인증에 실패했습니다.");
            return "redirect:/login";
        }

        // 4. 인가 코드 체크
        if (code == null || code.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "구글 로그인 인증 코드가 없습니다.");
            return "redirect:/login";
        }

        // 5. 토큰 발급
        String accessToken = googleService.getAccessToken(code);

        // 6. 사용자 프로필 조회 (신규 회원이면 DB INSERT 없이 임시 객체 반환)
        User loginUser = googleService.googleLoginProcess(accessToken);

        // 7. 신규 회원이면 추가 정보 입력 페이지로 이동
        if (loginUser.isNewUser()) {
            session.setAttribute("TEMP_OAUTH_USER", loginUser);
            return "redirect:/oauth/signup";
        }

        // 8. 기존 회원이면 로그인 세션 발급 후 메인으로 리다이렉트
        request.changeSessionId();

        SessionUser sessionUser = new SessionUser(
                loginUser.getMemberId(),
                loginUser.getNickname(),
                loginUser.getProvider(),
                loginUser.getRole()
        );
        // 9. 일반 로그인과 동일한 세션 구조
        session.setAttribute("checkLogin", true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, sessionUser);

        return "redirect:/";
    }
}