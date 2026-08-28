package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.SessionUser;
import first_project.recycle.domain.User;
import first_project.recycle.mapper.UserMapper;
import first_project.recycle.service.EcoPointHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class OAuthSignupController {

    private final UserMapper userMapper;
    private final EcoPointHistoryService ecoPointHistoryService;

    /**
     * 추가 정보 입력 화면 노출
     */
    @GetMapping("/oauth/signup")
    public String oauthSignupForm(HttpSession session, Model model) {
        User tempUser = (User) session.getAttribute("TEMP_OAUTH_USER");

        // 임시 세션이 없으면 비정상 접근이므로 로그인 화면으로 리다이렉트
        if (tempUser == null) {
            return "redirect:/login";
        }

        // 소셜에서 전달받은 기본 닉네임 바인딩
        model.addAttribute("defaultNickname", tempUser.getNickname());
        return "oauth-signup";
    }

    /**
     * 닉네임 및 성별 제출 처리 (성별은 DB 저장 X)
     */
    @PostMapping("/oauth/signup")
    public String processOAuthSignup(@RequestParam String nickname,
                                     @RequestParam(required = false) String gender,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }

        User tempUser = (User) session.getAttribute("TEMP_OAUTH_USER");
        if (tempUser == null) {
            return "redirect:/login";
        }

        // 닉네임 유효성 검사 (2~12자리 한글/영문/숫자)
        String nicknamePattern = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]{2,12}$";
        if (nickname == null || !nickname.matches(nicknamePattern)) {
            redirectAttributes.addFlashAttribute("error", "닉네임은 2~12자리의 한글, 영문, 숫자만 가능합니다.");
            return "redirect:/oauth/signup";
        }

        // 1. 닉네임 중복 사전 검증
        if (userMapper.countByNickname(nickname) > 0) {
            redirectAttributes.addFlashAttribute("error", "이미 사용 중인 닉네임입니다.");
            return "redirect:/oauth/signup";
        }

        // 2. 사용자가 수정한 닉네임만 세팅 (성별은 DB 미저장이므로 제외)
        tempUser.setNickname(nickname);

        // 3. DB에 최종 회원 데이터 저장
        userMapper.insertOAuthUser(tempUser);

        // 4. 신규 가입 에코포인트 지급
        ecoPointHistoryService.earnPoint(tempUser.getMemberId(), 100, "SIGNUP", tempUser.getMemberId());

        // 5. 임시 세션 제거 및 정식 로그인 세션 등록
        session.removeAttribute("TEMP_OAUTH_USER");
        request.changeSessionId();

        SessionUser sessionUser = new SessionUser(
                tempUser.getMemberId(),
                tempUser.getNickname(),
                tempUser.getProvider(),
                tempUser.getRole()
        );

        session.setAttribute("checkLogin", true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, sessionUser);

        redirectAttributes.addFlashAttribute("message", "회원가입을 축하합니다. 신규 가입 에코포인트 100p가 지급되었습니다.");
        return "redirect:/";
    }
}