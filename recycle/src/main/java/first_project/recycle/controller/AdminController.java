package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.User;
import first_project.recycle.dto.*;
import first_project.recycle.service.AdminService;
import first_project.recycle.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final BoardService boardService;
    private final AdminService adminService;

    // 관리자 메인페이지 > 전체 공지사항을 최신순으로 조회
    @GetMapping
    public String adminHome(Model model){

        //DashBoard 조회
        AdminDashBoardResponse dashBoard = adminService.findDashBoardData();
        model.addAttribute("dashBoard",dashBoard);
        //전체 공지사항 조회
        List<BoardListResponse> notices = boardService.getAllNotices();
        model.addAttribute("notices", notices);
        return "admin/adminHome";
    }

    // 공지사항 작성
    @PostMapping("/notices")
    public String createNotice(
            @ModelAttribute BoardCreateRequest request,
            HttpServletRequest httpServletRequest){

        //로그인 및 ADMIN 확인
        HttpSession session = httpServletRequest.getSession(false);

        User loginUser = (User)session.getAttribute(SessionConst.LOGIN_MEMBER);

        boardService.writeNotice(loginUser.getMemberId(),request);

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
                             @ModelAttribute BoardUpdateRequest request){

        boardService.updateNotice(boardId, request);
        return "redirect:/admin";
    }
    // 공지사항 삭제
    @PostMapping("/notices/{boardId}/delete")
    public String deleteNotice(@PathVariable Long boardId){
        boardService.deleteNotice(boardId);
        return "redirect:/admin";
    }

}
