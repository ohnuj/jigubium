package first_project.recycle.controller;


import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.BoardType;
import first_project.recycle.domain.Paging;
import first_project.recycle.dto.*;
import first_project.recycle.exception.ForbiddenException;
import first_project.recycle.service.BoardLikeService;
import first_project.recycle.service.BoardService;
import first_project.recycle.domain.User;
import first_project.recycle.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final BoardLikeService boardLikeService;





    /**
     * 게시글 목록 조회
     * 검색, 타입 분류, 페이징을 함께 처리
     */
    @GetMapping
    public String boardList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,
            HttpSession session,
            Model model) {

        Long memberId = null;

        // 내 글만 보기
        if (myPosts) {

            User loginUser =
                    (User) session.getAttribute(
                            SessionConst.LOGIN_MEMBER
                    );

            // 로그인하지 않은 사용자가 myPosts=true로 접근한 경우
            if (loginUser == null) {
                return "redirect:/login";
            }

            memberId = loginUser.getMemberId();
        }

        BoardPageResponse result =
                boardService.getBoards(
                        page,
                        keyword,
                        searchType,
                        boardType,
                        sort,
                        memberId
                );

        // 게시글 목록
        model.addAttribute(
                "boards",
                result.getBoards()
        );

        Paging paging =
                result.getPaging();

        // 페이징 정보
        model.addAttribute(
                "paging",
                paging
        );

        int blockSize = 5;

        int startPage =
                ((paging.getPage() - 1) / blockSize)
                        * blockSize + 1;

        int endPage =
                Math.min(
                        startPage + blockSize - 1,
                        paging.getTotalPages()
                );

        model.addAttribute(
                "startPage",
                startPage
        );

        model.addAttribute(
                "endPage",
                endPage
        );

        // 검색 조건 유지
        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "searchType",
                searchType
        );

        model.addAttribute(
                "boardType",
                boardType
        );

        model.addAttribute(
                "sort",
                sort
        );

        // 내 글 보기 상태 유지
        model.addAttribute(
                "myPosts",
                myPosts
        );

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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) BoardType boardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,
            HttpSession session) {

        // 게시글 존재 여부 먼저 확인
        BoardDetailResponse board =
                boardService.getBoardDetail(boardId);

        // 현재 세션에서 이미 조회한 게시글 목록
        Set<Long> viewedBoards =
                (Set<Long>) session.getAttribute(
                        "viewedBoards"
                );

        // 처음 조회하는 세션이면 Set 생성
        if (viewedBoards == null) {
            viewedBoards = new HashSet<>();
        }

        // 현재 게시글을 아직 조회하지 않았다면 조회수 증가
        if (!viewedBoards.contains(boardId)) {

            boardService.increaseViewCount(boardId);

            // 증가된 조회수를 화면에도 바로 반영
            board =
                    boardService.getBoardDetail(boardId);

            viewedBoards.add(boardId);

            session.setAttribute(
                    "viewedBoards",
                    viewedBoards
            );
        }

        // 게시글 상세 정보
        model.addAttribute(
                "board",
                board
        );


        // 댓글 페이징 조회
        CommentPageResponse commentResult =
                commentService.getCommentPage(
                        boardId,
                        commentPage
                );

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

        model.addAttribute(
                "previousBoard",
                boardService.getPreviousBoard(boardId)
        );

        model.addAttribute(
                "nextBoard",
                boardService.getNextBoard(boardId)
        );

        // 좋아요 수
        board.setLikeCount(
                boardLikeService.getLikeCount(boardId)
        );

        // 로그인한 사용자의 좋아요 여부
        if (loginUser != null) {
            board.setLiked(
                    boardLikeService.isLiked(
                            boardId,
                            loginUser.getMemberId()
                    )
            );
        }
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("boardType", boardType);
        model.addAttribute("sort", sort);
        model.addAttribute("myPosts", myPosts);

        return "board/detail";
    }

    /**
     * 게시글 수정 페이지
     */
    @GetMapping("/{boardId}/edit")
    public String editForm(
            @PathVariable Long boardId,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(name = "boardType", required = false)
            BoardType listBoardType,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean myPosts,

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
            throw new ForbiddenException(
                    "게시글을 수정할 권한이 없습니다."
            );
        }

        model.addAttribute("board",board);
        model.addAttribute("boardTypes", BoardType.values());
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        model.addAttribute("listBoardType", listBoardType);
        model.addAttribute("sort", sort);
        model.addAttribute("myPosts", myPosts);

        return "board/edit";
    }

    /**
     * 게시글 수정
     */
    @PostMapping("/{boardId}/edit")
    public String updateBoard(
            @PathVariable Long boardId,
            @ModelAttribute BoardUpdateRequest request,
            @RequestParam(required = false) List<MultipartFile> images,

            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String searchType,
            @RequestParam(name = "listBoardType", required = false)
            BoardType listBoardType,
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
                boardService.updateBoard(
                        boardId,
                        loginUser.getMemberId(),
                        request,
                        images
                );

        if (!updated) {
            throw new ForbiddenException(
                    "게시글을 수정할 권한이 없습니다."
            );
        }

        return redirectToDetail(
                boardId,
                page,
                keyword,
                searchType,
                listBoardType,
                sort,
                myPosts
        );
    }

    /**
     * 게시글 삭제
     */
    @PostMapping("/{boardId}/delete")
    public String deleteBoard(
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

        Long memberId = loginUser.getMemberId();

        boolean deleted =
                boardService.deleteBoard(
                        boardId,
                        memberId
                );

        if (!deleted) {
            throw new ForbiddenException(
                    "게시글을 삭제할 권한이 없습니다."
            );
        }

        return redirectToList(
                page,
                keyword,
                searchType,
                boardType,
                sort,
                myPosts
        );
    }

    /**
     * 게시글 기존 이미지 개별 삭제
     */
    @PostMapping("/{boardId}/images/{imageId}/delete")
    public String deleteImage(
            @PathVariable Long boardId,
            @PathVariable Long imageId,

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
                boardService.deleteBoardImage(
                        boardId,
                        imageId,
                        loginUser.getMemberId()
                );

        if (!deleted) {
            throw new ForbiddenException(
                    "이미지를 삭제할 권한이 없습니다."
            );
        }

        return redirectToEdit(
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
    private String redirectToEdit(
            Long boardId,
            int page,
            String keyword,
            String searchType,
            BoardType boardType,
            String sort,
            boolean myPosts) {

        String redirectUrl =
                UriComponentsBuilder
                        .fromPath("/boards/{boardId}/edit")
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
    private String redirectToList(
            int page,
            String keyword,
            String searchType,
            BoardType boardType,
            String sort,
            boolean myPosts) {

        String redirectUrl =
                UriComponentsBuilder
                        .fromPath("/boards")
                        .queryParam("page", page)
                        .queryParam("keyword", keyword)
                        .queryParam("searchType", searchType)
                        .queryParam("boardType", boardType)
                        .queryParam("sort", sort)
                        .queryParam("myPosts", myPosts)
                        .encode()
                        .toUriString();

        return "redirect:" + redirectUrl;
    }




}
