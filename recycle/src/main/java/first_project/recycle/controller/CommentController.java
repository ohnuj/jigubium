package first_project.recycle.controller;


import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.BoardType;
import first_project.recycle.domain.User;
import first_project.recycle.dto.CommentCreateRequest;
import first_project.recycle.dto.CommentUpdateRequest;
import first_project.recycle.exception.ForbiddenException;
import first_project.recycle.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

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

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,

            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(
                        SessionConst.LOGIN_MEMBER
                );

        if (loginUser == null) {
            return "redirect:/login";
        }

        commentService.createComment(
                boardId,
                loginUser.getMemberId(),
                request
        );

        return redirectToDetail(
                boardId,
                page,
                keyword,
                searchType,
                boardType,
                sort,
                myPosts
        );
    }

    /**
     * 댓글 수정
     */
    @PostMapping("/{commentId}/edit")
    public String updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @ModelAttribute CommentUpdateRequest request,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,

            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(
                        SessionConst.LOGIN_MEMBER
                );

        if (loginUser == null) {
            return "redirect:/login";
        }

        boolean updated =
                commentService.updateComment(
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

        return redirectToDetail(
                boardId,
                page,
                keyword,
                searchType,
                boardType,
                sort,
                myPosts
        );
    }

    /**
     * 댓글 삭제
     */
    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,

            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(
                        SessionConst.LOGIN_MEMBER
                );

        if (loginUser == null) {
            return "redirect:/login";
        }

        boolean deleted =
                commentService.deleteComment(
                        boardId,
                        commentId,
                        loginUser.getMemberId()
                );

        if (!deleted) {
            throw new ForbiddenException(
                    "댓글을 삭제할 권한이 없습니다."
            );
        }

        return redirectToDetail(
                boardId,
                page,
                keyword,
                searchType,
                boardType,
                sort,
                myPosts
        );
    }
    private String redirectToDetail(
            Long boardId,
            int page,
            String keyword,
            String searchType,
            BoardType boardType,
            String sort,
            boolean myPosts) {

        String redirectUrl =
                UriComponentsBuilder
                        .fromPath("/boards/{boardId}")
                        .queryParam("page", page)
                        .queryParam("keyword", keyword)
                        .queryParam("searchType", searchType)
                        .queryParam("boardType", boardType)
                        .queryParam("sort", sort)
                        .queryParam("myPosts", myPosts)
                        .buildAndExpand(boardId)
                        .encode()
                        .toUriString();

        return "redirect:" + redirectUrl;
    }

}
