package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
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

// 스프링 MVC 컨트롤러 등록
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

        // 구글 인가 코드 요청 URL 조합
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?response_type=code" // 응답 타입은 인가 코드(code)로 지정
                + "&client_id=" + clientId                                                 // 애플리케이션 클라이언트 ID 전달
                + "&redirect_uri=" + redirectUri                                           // 인가 완료 후 콜백받을 URI 전달
                + "&scope=" + scope                                                        // 접근 권한 범위
                + "&state=" + state                                                        // CALLBACK 후 검증을 위해 STATE 전달
                + "&prompt=select_account";                                                // 계정 선택 창 강제 노출

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

        // 1. 세션이 없으면 정상적인 구글 로그인 요청 X
        if (session == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "구글 로그인에 실패했습니다.");
            return "redirect:/login";
        }

        // 2. 로그인 시작 시 저장했던 state 조회
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

        // 4. 구글 로그인 취소 또는 인증 실패
        if (error != null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "구글 로그인이 취소되었거나 인증에 실패했습니다.");
            return "redirect:/login";
        }

        // 5. 정상 응답이지만 인가 code가 없는 경우
        if (code == null || code.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "구글 로그인 인증 코드가 없습니다.");
            return "redirect:/login";
        }

        // 6. state 검증 성공 후 Access Token 발급
        String accessToken = googleService.getAccessToken(code);

        // 7. 사용자 조회 및 회원가입 처리
        User loginUser = googleService.googleLoginProcess(accessToken);

        // 신규 구글 회원
        if (loginUser.isNewUser()) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "회원가입을 축하합니다. 신규 가입 에코포인트 100p가 지급되었습니다.");
        }

        // 8. login 성공 후 세션 ID 변경
        request.changeSessionId();

        // 9. 일반 로그인과 동일한 세션 구조
        session.setAttribute("checkLogin", true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginUser);
        return "redirect:/";
    }
}