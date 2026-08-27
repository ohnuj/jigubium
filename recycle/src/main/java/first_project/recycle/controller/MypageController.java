package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.*;
import first_project.recycle.service.BadgeService;
import first_project.recycle.service.BoardService;
import first_project.recycle.service.EcoPointHistoryService;
import first_project.recycle.service.MypageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor // final이 붙은 필드의 생성자를 자동 생성하여 스프링이 의존성을 주입해줌
@RequestMapping("/mypage") // 이 컨트롤러 내의 모든 URL 매핑 앞에 '/mypage'가 기본으로 붙음
@Controller // 스프링에게 이 클래스가 웹 요청을 처리하는 컨트롤러 빈임을 알림
public class MypageController {

    // 비즈니스 로직을 수행할 서비스 객체들을 선언 (불변성을 위해 final 사용)
    private final MypageService mypageService;
    private final EcoPointHistoryService ecoPointHistoryService;
    private final BoardService boardService;
    private final BadgeService badgeService;

    // 1. 디폴트 진입 -> 로그인 체크 후 내 정보(myinfo) 페이지로 이동
    @GetMapping("")
    public String mypageDefault() {
        return "redirect:/mypage/myInfo";
    }

    // 내 정보 화면
    @GetMapping("/myInfo")
    public String myInfo(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);


        // 세션에 담겨있는 로그인 유저 객체에서 식별자(PK)인 memberId를 꺼냄
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);        Long memberId = loginUser.getMemberId();

        // DB에서 회원의 최신 상세 정보 가져옴
        Member member = mypageService.getMemberInfo(memberId);
        // 만약 세션은 있는데 실제 DB에 회원 데이터가 없다면 세션을 지우고 로그인으로 이동
        if (member == null) {
            session.invalidate();
            return "redirect:/login";
        }
        // 현재 잔여 에코포인트 조회
        int currentPoint = ecoPointHistoryService.findCurrentBalance(memberId);

        // 누적 적립된 에코포인트 조회
        int totalEarnPoint = ecoPointHistoryService.findTotalPoint(memberId);

        // 누적 에코포인트로 뱃지 조회
        Badge currentBadge = badgeService.findCurrentBadge(totalEarnPoint);

        // Thymeleaf 템플릿(HTML)에서 사용할 수 있도록 Model 객체에 데이터 바인딩
        model.addAttribute("member", member); // 회원 프로필 정보 객체
        model.addAttribute("currentTab", "myInfo"); // 사이드바에서 '내 정보' 메뉴 활성화 플래그
        model.addAttribute("currentPoint", currentPoint); // 현재 포인트
        model.addAttribute("totalPoint", totalEarnPoint); // 누적 포인트(등급 산정용)
        model.addAttribute("currentBadge", currentBadge); // 뱃지

        return "mypage/myInfo";
    }

    // 2. 비밀번호 입력 화면 (1번 탭 디폴트)
    @GetMapping("/memberInfoConfirm")
    public String memberInfoConfirmForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        // 카카오 회원은 인증 없이 회원정보 수정 화면 (닉네임만 수정 가능)
        if ("KAKAO".equals(loginUser.getProvider())) {
            return "redirect:/mypage/updateMemberInfo";
        }
        session.removeAttribute("mypageVerified");
        model.addAttribute("currentTab", "info");
        return "mypage/checkPassword";
    }

    // 3. 비밀번호 확인 처리 (POST)
    @PostMapping("/checkPassword")
    public String checkPassword(@RequestParam("password") String password,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);

        // 이메일 대신 memberId 추출
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);        Long memberId = loginUser.getMemberId();

        // 서비스 계층을 통해 DB 비밀번화와 일치하는지 비교 검증
        boolean isMatch = mypageService.verifyPassword(memberId, password);

        // 비밀번호 틀렸을때
        if (!isMatch) {
            // 새로고침 시 사라지는 1회성 플래시 에러 메세지 설정
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "비밀번호가 일치하지 않습니다."
            );

            // 실패 시 비밀번호 입력 화면으로 다시 이동
            return "redirect:/mypage/memberInfoConfirm";
        }
        // 비밀번호 통과 플래그를 세션에 기록
        session.setAttribute("mypageVerified", true);
        // 검증 성공 시 정보 수정 화면 URL로 이동
        return "redirect:/mypage/updateMemberInfo";
    }

    // 4. 회원정보 수정 화면 (GET)
    @GetMapping("/updateMemberInfo")
    public String updateMemberInfoForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (!"KAKAO".equals(loginUser.getProvider())) {
            //pw check을 하고 넘어왔는지 확인, 브라우저를 통해 바로 update하러 오는 것을 방지하기 위함
            Boolean verified = (Boolean) session.getAttribute("mypageVerified");
            if (!Boolean.TRUE.equals(verified)) {
                return "redirect:/mypage/memberInfoConfirm";
            }
        }
        // 이메일 대신 memberId 추출
        Long memberId = loginUser.getMemberId();

        Member member = mypageService.getMemberInfo(memberId);

        if (member == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("member", member);
        model.addAttribute("currentTab", "info");
        return "mypage/updateMemberInfo";
    }

    // 5. 회원정보 수정 처리 (POST)
    @PostMapping("/updateMemberInfo")
    public String updateMemberInfo(@ModelAttribute MemberInfo memberinfo,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        // LOCAL 회원만 비밀번호 검증
        if (!"KAKAO".equals(loginUser.getProvider())) {
            Boolean verified =
                    (Boolean) session.getAttribute(
                            "mypageVerified");

            if (!Boolean.TRUE.equals(verified)) {
                return "redirect:/mypage/memberInfoConfirm";
            }
        }

        Long memberId = loginUser.getMemberId();

        /*
         * LOCAL 회원이 새 비밀번호를 입력한 경우에만
         * 새 비밀번호와 확인 비밀번호 일치 여부 검증
         */
        if (!"KAKAO".equals(loginUser.getProvider())
                && memberinfo.getNewpassword() != null
                && !memberinfo.getNewpassword().isBlank()) {

            if (!memberinfo.getNewpassword()
                    .equals(memberinfo.getNewpasswordconfirm())) {

                redirectAttributes.addFlashAttribute(
                        "errorMsg",
                        "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");

                return "redirect:/mypage/updateMemberInfo";
            }
        }

        // 서비스 호출 -> DB 업데이트 (닉네임 변경 및 비밀번호 변경)
        mypageService.updateMemberInfo(memberId, memberinfo);

        // 세션에 저장된 닉네임 동기화 (상단 GNB나 화면 세션에 즉시 반영하기 위해 세션 객체 동기화)
        if (memberinfo.getNickname() != null
                && !memberinfo.getNickname().isBlank()) {
            loginUser.setNickname(memberinfo.getNickname());
            session.setAttribute(SessionConst.LOGIN_MEMBER, loginUser);
        }
        // 회원정보 수정 끝났으면 비밀번호 확인 상태 제거
        session.removeAttribute("mypageVerified");
        // 1. 성공 메시지 전달
        redirectAttributes.addFlashAttribute(
                "successMsg",
                "회원정보가 성공적으로 수정되었습니다."
        );

        // 2. 메인페이지("/")로 이동
        return "redirect:/?updated=true";
    }

    // 6. 회원 탈퇴 화면 (GET)
    @GetMapping("/memberDelete")
    public String memberDelete(HttpServletRequest request,Model model) {

        HttpSession session = request.getSession(false);
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);        Long memberId = loginUser.getMemberId();
        Member member = mypageService.getMemberInfo(memberId);

        model.addAttribute("member", member);
        // 사이드바 '회원 탈퇴' 탭 강조
        model.addAttribute("currentTab", "withdraw");
        return "mypage/memberDelete";
    }

    // 7. 회원 탈퇴 처리 (POST)
    // 비밀번호 재검증 후 DB에서 회원 데이터 영구 삭제 및 세션 무효화
    @PostMapping("/memberDelete")
    public String deleteMember(@RequestParam(value = "password", required = false) String password,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);


        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);        Long memberId = loginUser.getMemberId(); // memberId 추출
        // 서비스 호출 -> 비밀번호 확인후 일치하면 삭제 진행
        boolean isDeleted = mypageService.deleteMember(memberId, password);

        // 비밀번호 불일치로 삭제 실패시
        if (!isDeleted) {
            // 카카오 유저
            if ("KAKAO".equals(loginUser.getProvider())) {
                redirectAttributes.addFlashAttribute(
                        "errorMsg",
                        "회원 탈퇴에 실패했습니다.");
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMsg", "비밀번호가 일치하지 않습니다.");
            }
            return "redirect:/mypage/memberDelete";
        }

        // 탈퇴 성공 시 세션 파기 후 로그인 창으로 이동
        session.invalidate();
        return "redirect:/login?deleted=true";
    }

    // 회원 활동 조회
    @GetMapping("/myActivity")
    public String myActivity(
            // 요청받은 현재 페이지 번호를 기본값으로 1페이지로 하겠다
            @RequestParam(name = "page", defaultValue = "1") int page,
            HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);


        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);        Long memberId = loginUser.getMemberId();

        // 한 페이지당 보여줄 게시물 개수
        int pageSize = 10;

        // 1. 포인트 내역의 전체 개수를 구해서 전체 페이지 수를 계싼
        int pointPageCount = mypageService.getPointPageCount(memberId);
        // 13건이면 1.3 -> 올림하여 2페이지로 계산
        int pagingPages = (int) Math.ceil((double) pointPageCount / pageSize);
        // 내역이 0건이어도 기본 1페이지는 유지
        if (pagingPages == 0) pagingPages = 1;

        // 2. 에코포인트 변동 내역 조회 -> 현재 페이지 번호와 페이지 크기를 기반으로
        // LIMIT/OFFSET 페이징 목록 조회
        List<EcoPointHistory> pointHistoryList = mypageService.getPointHistoryPaging
                (memberId, page, pageSize);

        // 3. 현재 보유 포인트
        int currentPoint = ecoPointHistoryService.findCurrentBalance(memberId);

        // 4. 작성한 총 게시글 수 조회
        int boardCount = boardService.getBoardCount(memberId);

        // 5. 작성한 총 댓글 수 조회
        int commentCount = mypageService.getCommentCount(memberId);

        // 6. 리워드 상품명 및 보유 갯수
        List<Map<String, Object>> myRewardList = mypageService.getMyReward(memberId);

        // 모델에 데이터 바인딩
        model.addAttribute("boardCount", boardCount); // 게시글 수
        model.addAttribute("pointHistoryList", pointHistoryList); // 포인트 내역(10개단위)
        model.addAttribute("currentTab", "activity"); // 사이드바 활성화 탭
        model.addAttribute("currentPoint", currentPoint); // 현재 포인트
        model.addAttribute("rewardList", myRewardList); // 보유 리워드 목록
        model.addAttribute("commentCount", commentCount); // 댓글 수

        // 페이징 관련 변수 전달
        model.addAttribute("currentPage", page); // 현재 페이지
        model.addAttribute("pagingPages", pagingPages); // 존채 페이지 수(버튼 생성용)
        model.addAttribute("pointPageCount", pointPageCount); // 전체 변동 건수

        return "mypage/myActivity";
    }


}