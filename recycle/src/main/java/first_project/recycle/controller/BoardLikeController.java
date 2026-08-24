package first_project.recycle.controller;


import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.BoardType;
import first_project.recycle.domain.User;
import first_project.recycle.service.BoardLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

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

        boardLikeService.toggleLike(
                boardId,
                loginUser.getMemberId()
        );

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
