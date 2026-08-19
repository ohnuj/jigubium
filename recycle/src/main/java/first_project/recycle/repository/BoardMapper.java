package first_project.recycle.repository;

import first_project.recycle.domain.Board;
import first_project.recycle.domain.BoardType;
import first_project.recycle.dto.BoardListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
//    게시판작성
    int insertBoard(Board board);

    // 메인페이지에 보여줄 최신 게시글 조회
    List<BoardListResponse> findRecentBoards();

    // 검색 조건에 맞는 게시글 개수
    int countBoards(
            @Param("keyword") String keyword,
            @Param("boardType")BoardType boardType
            );

    // 검색 + 타입 + 페이징 게시글 조회
    List<BoardListResponse> findBoards(
            @Param("keyword") String keyword,
            @Param("boardType") BoardType boardType,
            @Param("offset") int offset,
            @Param("size") int size

    );
}
