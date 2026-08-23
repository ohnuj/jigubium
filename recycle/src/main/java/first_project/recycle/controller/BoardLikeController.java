package first_project.recycle.controller;


import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.User;
import first_project.recycle.service.BoardLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards/{boardId}/like")
public class BoardLikeController {

    private final BoardLikeService boardLikeService;

    /**
     * 좋아요 등록 / 취소
     */
    @PostMapping
    public String toggleLike(
            @PathVariable Long boardId,
            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(
                        SessionConst.LOGIN_MEMBER
                );

        if (loginUser == null) {
            return "redirect:/login";
        }

        boardLikeService.toggleLike(
                boardId,
                loginUser.getMemberId()
        );

        return "redirect:/boards/" + boardId;
    }

}
