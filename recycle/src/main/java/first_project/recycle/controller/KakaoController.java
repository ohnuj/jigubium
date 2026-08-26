package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.User;
import first_project.recycle.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 스프링 MVC 컨트롤러 등록
@Controller
// final 필드(kakaoService) 생성자 주입
@RequiredArgsConstructor
public class KakaoController {

    // 카카오 OAuth API 통신 및 DB 처리를 담당하는 서비스 객체 의존성 주입
    private final KakaoService kakaoService;

    // application.properties에 설정된 카카오 REST API 키 주입
    @Value("${kakao.client-id}")
    private String clientId;

    // application.properties에 설정된 인가 코드 수신용 콜백 URL 주입
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 카카오 로그인 시작 엔드포인트
     * 브라우저를 카카오 인증 서버의 로그인/동의 요청 페이지로 리다이렉트
     */
    @GetMapping("/oauth/kakao")
    public String kakaoLogin() {
        // 카카오 인가 코드 요청 URL 조합
        String authUrl = "https://kauth.kakao.com/oauth/authorize?response_type=code" // 응답 타입은 인가 코드(code)로 지정
                + "&client_id=" + clientId                                           // 애플리케이션 REST API 키 전달
                + "&redirect_uri=" + redirectUri                                     // 인가 완료 후 콜백받을 URI 전달
                + "&prompt=login";                                                   // 기존 로그인 세션이 있어도 매번 계정 로그인 화면 강제 노출

        // 조합된 카카오 인증 URL로 사용자 브라우저 강제 이동
        return "redirect:" + authUrl;
    }

    /**
     * 카카오 로그인 콜백 엔드포인트
     * 사용자가 카카오 로그인을 완료했을 때 카카오 인증 서버가 인가 코드(code)를 담아 호출
     */
    @GetMapping("/oauth/kakao/callback")
    public String kakaoCallback(@RequestParam String code,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        // 1. 전달받은 인가 코드(code)를 카카오 인증 서버에 전달하여 Access Token 발급
        String accessToken = kakaoService.getAccessToken(code);

        // 2. 발급받은 Access Token으로 사용자 프로필을 조회하고, DB 회원가입/조회 후 User 객체 반환
        User loginUser = kakaoService.kakaoLoginProcess(accessToken);

        if (loginUser.isNewUser()) {
            redirectAttributes.addFlashAttribute("message", "회원가입을 축하합니다! 신규 가입 에코포인트 100P가 지급되었습니다.");
        }
        // 3. 현재 요청의 HTTP 세션 조회 (없으면 신규 생성)
        HttpSession session = request.getSession(false);
        if (session != null) {
            request.changeSessionId();
        } else {
            session = request.getSession(true);
        }

        // 4. 일반 로그인과 동일하게 로그인 상태 플래그 세션 저장
        session.setAttribute("checkLogin", true);

        // 5. 세션에 로그인 성공한 회원 정보 객체 바인딩
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginUser);

        // 6. 모든 로그인 처리가 완료된 후 메인 페이지('/')로 리다이렉트
        return "redirect:/";
    }
}