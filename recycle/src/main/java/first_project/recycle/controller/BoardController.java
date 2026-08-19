package first_project.recycle.controller;


import first_project.recycle.domain.BoardType;
import first_project.recycle.dto.BoardPageResponse;
import first_project.recycle.service.BoardService;
import first_project.recycle.domain.User;
import first_project.recycle.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 목록 조회
     * 검색, 타입 분류, 페이징을 함께 처리
     */
    @GetMapping
    public String boardList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)BoardType boardType,
            Model model) {

        BoardPageResponse result = boardService.getBoards(page,keyword,boardType);

        // 게시글 목록
        model.addAttribute("boards", result.getBoards());

        // 페이징 정보
        model.addAttribute("paging", result.getPaging());

        // 검색 후 검색어 유지
        model.addAttribute("keyword", keyword);

        // 검색 후 선택한 게시판 타입 유지
        model.addAttribute("boardType", boardType);

        return "board/list";
    }

}


    }
}
