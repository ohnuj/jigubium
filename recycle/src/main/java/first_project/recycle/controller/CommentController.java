package first_project.recycle.controller;


import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.BoardType;
import first_project.recycle.domain.User;
import first_project.recycle.dto.CommentCreateRequest;
import first_project.recycle.dto.CommentUpdateRequest;
import first_project.recycle.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
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
            @ModelAttribute CommentCreateRequest commentRequest,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int commentPage,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,

            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser =
                (User) session.getAttribute(
                        SessionConst.LOGIN_MEMBER
                );

        commentService.createComment(
                boardId,
                loginUser.getMemberId(),
                commentRequest
        );

        return redirectToDetail(boardId,
                page,
                commentPage,
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
            @ModelAttribute CommentUpdateRequest commentRequest,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int commentPage,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,

            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser =
                (User) session.getAttribute(
                        SessionConst.LOGIN_MEMBER
                );

        commentService.updateComment(
                        boardId,
                        commentId,
                        loginUser.getMemberId(),
                        commentRequest
                );
        return redirectToDetail(
                boardId,
                page,
                commentPage,
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
            @RequestParam(defaultValue = "1") int commentPage,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,

            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser =
                (User) session.getAttribute(
                        SessionConst.LOGIN_MEMBER
                );

         commentService.deleteComment(
                        boardId,
                        commentId,
                        loginUser.getMemberId()
                );

        return redirectToDetail(
                boardId,
                page,
                commentPage,
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
            int commentPage,
            String keyword,
            String searchType,
            BoardType boardType,
            String sort,
            boolean myPosts) {

        String redirectUrl =
                UriComponentsBuilder
                        .fromPath("/boards/{boardId}")
                        .queryParam("page", page)
                        .queryParam("commentPage", commentPage)
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
