package first_project.recycle.controller;


import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.User;
import first_project.recycle.dto.CommentCreateRequest;
import first_project.recycle.dto.CommentUpdateRequest;
import first_project.recycle.exception.ForbiddenException;
import first_project.recycle.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 등록
     */
    @PostMapping
    public String createComment(
            @PathVariable Long boardId,
            @ModelAttribute CommentCreateRequest request,
            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        // 비로그인 사용자는 로그인 페이지로 이동
        if (loginUser == null) {
            return "redirect:/login";
        }

        commentService.createComment(
                boardId,
                loginUser.getMemberId(),
                request
        );

        return "redirect:/boards/" + boardId;
    }

    /**
     * 댓글 수정
     */
    @PostMapping("/{commentId}/edit")
    public String updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @ModelAttribute CommentUpdateRequest request,
            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        boolean updated = commentService.updateComment(
                boardId,
                commentId,
                loginUser.getMemberId(),
                request
        );

        if (!updated) {
            throw new ForbiddenException(
                    "댓글을 수정할 권한이 없습니다."
            );
        }

        return "redirect:/boards/" + boardId;
    }

    /**
     * 댓글 삭제
     */
    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        boolean deleted = commentService.deleteComment(
                boardId,
                commentId,
                loginUser.getMemberId()
        );

        if (!deleted) {
            throw new ForbiddenException(
                    "댓글을 삭제할 권한이 없습니다."
            );
        }

        return  "redirect:/boards/" + boardId;
    }

}
