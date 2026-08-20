package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import first_project.recycle.domain.User;
import first_project.recycle.service.MypageService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@RequestMapping("/mypage")
@Controller
public class MypageController {

    private final MypageService mypageService;

    // 1. 디폴트 진입 -> 로그인 체크 후 비밀번호 확인 페이지로 이동
    @GetMapping("")
    public String mypageDefault(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }
        return "redirect:/mypage/memberinfoconfirm";
    }

    // 2. 비밀번호 입력 화면 (1번 탭 디폴트)
    @GetMapping("/memberinfoconfirm")
    public String memberInfoConfirmForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentTab", "info");
        return "mypage/checkpassword";
    }

    // 3. 비밀번호 확인 처리 (POST)
    @PostMapping("/checkpassword")
    public String checkPassword(@RequestParam("password") String password,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        // 세션에서 User 객체를 꺼내 이메일 추출
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_MEMBER);
        String loginEmail = loginUser.getEmail();

        // DB 조회를 통해 비밀번호 검증
        boolean isMatch = mypageService.verifyPassword(loginEmail, password);

        if (!isMatch) {
            redirectAttributes.addFlashAttribute("errorMsg", "회원정보가 일치하지 않습니다.");
            return "redirect:/mypage/memberinfoconfirm";
        }

        session.setAttribute("mypageVerified", true);
        return "redirect:/mypage/updatememberinfo";
    }

    // 4. 회원정보 수정 화면 (GET)
    @GetMapping("/updatememberinfo")
    public String updateMemberInfoForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_MEMBER);
        String loginEmail = loginUser.getEmail();

        Member member = mypageService.getMemberInfo(loginEmail);

        model.addAttribute("member", member);
        model.addAttribute("currentTab", "info");
        return "mypage/updatememberinfo";
    }

    // 5. 회원정보 수정 처리 (POST)
    @PostMapping("/updatememberinfo")
    public String updateMemberInfo(@ModelAttribute MemberInfo memberinfo,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_MEMBER);
        String loginEmail = loginUser.getEmail();

        // 새 비밀번호 일치 여부 검증
        if (memberinfo.getNewpassword() != null && !memberinfo.getNewpassword().isBlank()) {
            if (!memberinfo.getNewpassword().equals(memberinfo.getNewpasswordconfirm())) {
                redirectAttributes.addFlashAttribute("errorMsg", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                return "redirect:/mypage/updatememberinfo";
            }
        }

        // DB 업데이트
        mypageService.updateMemberInfo(loginEmail, memberinfo);

        // 세션에 저장된 닉네임 동기화
        if (memberinfo.getNickname() != null && !memberinfo.getNickname().isBlank()) {
            loginUser.setNickname(memberinfo.getNickname());
            session.setAttribute(SessionConst.LOGIN_MEMBER, loginUser);
        }

        // 1. 성공 메시지 전달
        redirectAttributes.addFlashAttribute("successMsg", "회원정보가 성공적으로 수정되었습니다.");

        // 2. 메인페이지("/")로 이동
        return "redirect:/?updated=true";
    }

    // 6. 회원 탈퇴 화면 (GET)
    @GetMapping("/memberdelete")
    public String memberDelete(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentTab", "withdraw");
        return "mypage/memberdelete";
    }

    // 7. 회원 탈퇴 처리 (POST)
    @PostMapping("/memberdelete")
    public String deleteMember(@RequestParam("password") String password,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_MEMBER);
        String loginEmail = loginUser.getEmail();

        boolean isDeleted = mypageService.deleteMember(loginEmail, password);

        if (!isDeleted) {
            redirectAttributes.addFlashAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage/memberdelete";
        }

        // 탈퇴 성공 시 세션 파기 후 이동
        session.invalidate();
        return "redirect:/login?deleted=true";
    }

    // 회원 활동 조회
 //   @GetMapping("/activity")
  //  public String memberActicity(HttpServletRequest request, Model model) {
  //      HttpSession session = request.getSession(false);
   //     if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
   //     return "redirect:/login";
   //     }

}