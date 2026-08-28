package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.RewardExchange;
import first_project.recycle.domain.SessionUser;
import first_project.recycle.dto.*;
import first_project.recycle.service.AdminService;
import first_project.recycle.service.BoardService;
import first_project.recycle.service.RewardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final BoardService boardService;
    private final AdminService adminService;
    private final RewardService rewardService;

    // 관리자 메인페이지 > 전체 공지사항을 최신순으로 조회
    @GetMapping
    public String adminHome(Model model){

        //DashBoard 조회
        AdminDashBoardResponse dashBoard = adminService.findDashBoardData();
        model.addAttribute("dashBoard",dashBoard);
        //전체 공지사항 조회
        List<BoardListResponse> notices = boardService.getAllNotices();
        model.addAttribute("notices", notices);
        //전체 건의글 조회
        List<BoardListResponse> suggestions = boardService.getAllSuggestions();
        model.addAttribute("suggestions", suggestions);

        //리워드 교환 요청 조회
        List<RewardExchange> rewardExchanges = rewardService.findAdminRewardSummary();
        model.addAttribute("rewardExchanges", rewardExchanges);

        // 개수
        model.addAttribute("noticeCount", boardService.getNoticeCount());

        model.addAttribute("suggestionCount", boardService.getSuggestionCount());

        model.addAttribute("requestedCount", rewardService.countRequested());
        return "admin/adminHome";
    }

    // 공지사항 작성
    @PostMapping("/notices")
    public String createNotice(
            @ModelAttribute BoardCreateRequest boardRequest,
            HttpServletRequest httpServletRequest){

        // 현재 로그인한 관리자 정보 조회
        HttpSession session = httpServletRequest.getSession(false);

        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);

        boardService.writeNotice(loginUser.getMemberId(),boardRequest);

        return "redirect:/admin";
    }

    // 공지사항 수정 화면
    @GetMapping("/notices/{boardId}/edit")
    public String editNoticeForm(@PathVariable Long boardId, Model model){

        BoardDetailResponse notice = boardService.getNotice(boardId);

        model.addAttribute("notice", notice);
        return "admin/noticeEdit";
    }

    // 공지사항 수정
    @PostMapping("/notices/{boardId}/edit")
    public String editNotice(@PathVariable Long boardId,
                             @ModelAttribute BoardUpdateRequest boardRequest){

        boardService.updateNotice(boardId, boardRequest);
        return "redirect:/admin";
    }
    // 공지사항 삭제
    @PostMapping("/notices/{boardId}/delete")
    public String deleteNotice(@PathVariable Long boardId){
        boardService.deleteNotice(boardId);
        return "redirect:/admin";
    }


    // 관리자 건의글 학인
    @PostMapping("/suggestions/{boardId}/check")
    public String checkSuggestion(@PathVariable Long boardId){
        boardService.checkSuggestionByAdmin(boardId);
        return "redirect:/boards/" + boardId;
    }

    // 관리자 > 리워드 교환 요청 완료
    @PostMapping("/rewards/{exchangeId}/complete")
    public String completeRewardExchange(
            @PathVariable Long exchangeId,
            RedirectAttributes redirectAttributes){

        rewardService.completeExchange(exchangeId);

        redirectAttributes.addFlashAttribute(
                "message",
                "리워드 교환 요청을 완료 처리했습니다.");
        return "redirect:/admin/rewards";
    }

    // 관리자 > 리워드 교환 요청 거절
    @PostMapping("/rewards/{exchangeId}/reject")
    public String rejectRewardExchange(
            @PathVariable Long exchangeId,
            RedirectAttributes redirectAttributes){

        rewardService.rejectExchange(exchangeId);

        redirectAttributes.addFlashAttribute(
                "message",
                "리워드 교환 요청을 거절했습니다.");
        return "redirect:/admin/rewards";
    }

    @GetMapping("/rewards")
    public String rewardManagement(Model model){
        List<RewardExchange> rewardExchanges = rewardService.findAllExchange();
        model.addAttribute("rewardExchanges", rewardExchanges);
        model.addAttribute("requestedRewardCount", rewardService.countRequested());
        return "admin/rewards";
    }
}
