package first_project.recycle.controller;

import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import first_project.recycle.service.MypageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@RequestMapping("/mypage") // 컨트롤러내 모든 매핑 "/mypage" url 자동 기입
@Controller
public class MypageContoller {

    private final MypageService mypageService;

    // 메인페이지 디폴트 화면 - 비밀번호 입력(회원정보 수정)
    @GetMapping("")
    public String mypageDefault(HttpServletRequest request){
        // 세션 있으면(로그인 되어있으면) null반환
        HttpSession session = request.getSession(false);

        if(session == null || session.getAttribute("loginEmail") == null) {
            return "redirect:/login";
        }
        return "redirect:/mypage/memberinfoconfirm";
    }

    // 회원정보 수정 탭
    @GetMapping("/memberinfoconfirm")
    public String memberInfoConfirmForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loginEmail") == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentTab", "info"); // 사이드바에서 1번 탭 활성화
        return "mypage/checkpassword"; // 비밀번호 입력받는 HTML
    }

    // 회원정보 수정 (페이지 이동을 위한 비밀번호 확인)
    @PostMapping("/checkpassword")
    public String checkPassword(
            @RequestParam("password") String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loginEmail") == null) {
            return "redirect:/login";
        }

        // 현재 로그인한 회원의 이메일
        String loginEmail = (String) session.getAttribute("loginEmail");

        // DB에서 현재 회원의 비밀번호 확인
        boolean isMatch = mypageService.verifyPassword(loginEmail, password);

        // 비밀번호 불일치
        if (!isMatch) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "회원정보가 일치하지 않습니다."
            );
            return "redirect:/mypage/memberinfoconfirm";
        }

        // 비밀번호 확인 성공
        session.setAttribute("mypageVerified", true);
        return "redirect:/mypage/updatememberinfo";
    }

        // 회원정보 수정화면
        @GetMapping("/updatememberinfo")
        public String updateMemberInfoFo( HttpServletRequest request, Model model) {

            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("loginEmail") == null) {
                return "redirect:/login";
            }

            String loginEmail = (String) session.getAttribute("loginEmail");

            // 회원정보 조회
            Member member = mypageService.getMemberInfo(loginEmail);

            model.addAttribute("member", member);
            model.addAttribute("currentTab", "info");

            return "mypage/updatememberinfo";
        }

    // 회원정보 수정
    @PostMapping("/updatememberinfo")
    public String updateMemberInfo(
            @ModelAttribute MemberInfo memberinfo,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loginEmail") == null) {
            return "redirect:/login";
        }

        String loginEmail = (String) session.getAttribute("loginEmail");

        // 새 비밀번호를 입력한 경우에만
        // 새 비밀번호와 확인값 비교
        if (memberinfo.getNewpassword() != null && !memberinfo.getNewpassword().isBlank()) {

            if (!memberinfo.getNewpassword().equals(memberinfo.getNewpasswordconfirm())) {

                redirectAttributes.addFlashAttribute(
                        "errorMsg",
                        "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."
                );
                return "redirect:/mypage/updatememberinfo";
            }
        }

        // 닉네임 / 비밀번호 DB 수정
        mypageService.updateMemberInfo(loginEmail,memberinfo);

        redirectAttributes.addFlashAttribute(
                "successMsg",
                "회원정보가 수정되었습니다."
        );
        return "redirect:/mypage";
    }


    // 회원 포인트 활동(게시판 등록 수 및 포인트 조회)
   // @GetMapping("/memberactivity")
   // public String memberActivity(){}

    // 회원 탈퇴 기능
    @GetMapping("/memberdelete")
    public String memberDelete(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginEmail") == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentTab", "withdraw");
        return "mypage/withdraw";
}
    // 7. 회원 탈퇴 처리 (POST)
    @PostMapping("/memberdelete")
    public String deleteMember(@RequestParam("password") String password,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginEmail") == null) {
            return "redirect:/login";
        }

        String loginEmail = (String) session.getAttribute("loginEmail");
        boolean isDeleted = mypageService.deleteMember(loginEmail, password);

        if (!isDeleted) {
            redirectAttributes.addFlashAttribute("errorMsg", "비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage/memberdelete"; // URL 불일치 수정됨
        }

        session.invalidate(); // 세션 만료
        return "redirect:/login?deleted=true";
    }
}