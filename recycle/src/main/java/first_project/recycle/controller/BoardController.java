package first_project.recycle.controller;


import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.BoardType;
import first_project.recycle.dto.*;
import first_project.recycle.service.BoardService;
import first_project.recycle.domain.User;
import first_project.recycle.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;



    /**
     * 게시글 목록 조회
     * 검색, 타입 분류, 페이징을 함께 처리
     */
    @GetMapping
    public String boardList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false)BoardType boardType,
            Model model) {

        BoardPageResponse result =
                boardService.getBoards(
                        page,
                        keyword,
                        searchType,
                        boardType
                );

        // 게시글 목록
        model.addAttribute("boards", result.getBoards());

        // 페이징 정보
        model.addAttribute("paging", result.getPaging());

        // 검색 후 검색어 유지
        model.addAttribute("keyword", keyword);

        model.addAttribute("searchType", searchType);

        // 검색 후 선택한 게시판 타입 유지
        model.addAttribute("boardType", boardType);

        return "board/list";
    }

    /**
     * 게시글 작성 페이지
     */
    @GetMapping("/write")
    public String writeForm(Model model,
                            HttpSession session){

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        // 로그인하지 않은 사용자는 로그인 페이지로 이동
        if (loginUser == null) {
            return "redirect:/login";
        }
        // 작성 페이지에서 게시판 타입 선택에 사용
        model.addAttribute("boardTypes", BoardType.values());

        return "board/write";
    }

    /**
     * 게시글 등록
     */
    @PostMapping
    public String write(
            @ModelAttribute BoardCreateRequest request,
            @RequestParam(required = false) List<MultipartFile> images,
            HttpSession session) {

        // 로그인 기능 연동 전 임시 회원 ID
        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginUser == null) {
            return "redirect:/login";
        }


        Long boardId = boardService.write(
                loginUser.getMemberId(),
                request,
                images
        );

        // 등록한 게시글 상세 페이지로 이동
        return "redirect:/boards/" + boardId;
    }

    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{boardId}")
    public String boardDetail(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "1") int commentPage,
            Model model,
            HttpSession session) {

        // 게시글 상세 정보
        model.addAttribute(
                "board",
                boardService.getBoard(boardId)
        );

        // 댓글 페이징 조회
        CommentPageResponse commentResult = commentService.getCommentPage(boardId, commentPage);

        // 해당 게시글의 댓글 목록
        model.addAttribute(
                "comments",
                commentResult.getComments()
        );
        model.addAttribute("commentPaging",
                            commentResult.getPaging());

        // 로그인 사용자
        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        model.addAttribute("loginUser", loginUser);

        return "board/detail";
    }

    /**
     * 게시글 수정 페이지
     */
    @GetMapping("/{boardId}/edit")
    public String editForm(
            @PathVariable Long boardId,
            Model model,
            HttpSession session) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        BoardDetailResponse board =
                boardService.getBoard(boardId);

        // 작성자 본인만 수정 페이지 접근 가능
        if (!Objects.equals(
                board.getMemberId(),
                loginUser.getMemberId())) {
            throw new IllegalArgumentException(
                    "게시글을 수정할 권한이 없습니다."
            );
        }

        model.addAttribute("board",board);
        model.addAttribute("boardTypes", BoardType.values());

        return "board/edit";
    }

    /**
     * 게시글 수정
     */
    @PostMapping("/{boardId}/edit")
    public String updateBoard(
            @PathVariable Long boardId,
            @ModelAttribute BoardUpdateRequest request,
            HttpSession session) {

        // 로그인 기능 연동 전 임시 회원 ID
        User loginUser =
                (User)  session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        Long memberId = loginUser.getMemberId();

        boolean updated =
                boardService.updateBoard(
                        boardId,
                        memberId,
                        request
                );

        if (!updated) {
            throw new IllegalArgumentException(
                    "게시글을 수정할 권한이 없습니다."
            );
        }

        return "redirect:/boards/" + boardId;
    }

    /**
     * 게시글 삭제
     */
    @PostMapping("/{boardId}/delete")
    public String deleteBoard(
            @PathVariable Long boardId,
            HttpSession session) {

        // 로그인 연동 전 임시 회원 ID
        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        Long memberId = loginUser.getMemberId();

        boolean deleted =
                boardService.deleteBoard(boardId, memberId);

        if (!deleted) {
            throw new IllegalArgumentException(
                    "게시글을 삭제할 권한이 없습니다."
            );
        }

        return "redirect:/boards";
    }



}
