package first_project.recycle.repository;

import first_project.recycle.domain.Board;
import first_project.recycle.domain.BoardType;
import first_project.recycle.dto.BoardDetailResponse;
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
            @Param("searchType") String searchType,
            @Param("boardType")BoardType boardType,
            @Param("memberId") Long memberId
            );

    // 검색 + 타입 + 페이징 게시글 조회
    List<BoardListResponse> findBoards(
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("boardType") BoardType boardType,
            @Param("sort") String sort,
            @Param("memberId") Long memberId,
            @Param("offset") int offset,
            @Param("size") int size

    );

    // 상세 조회
    BoardDetailResponse findById(Long boardId);

    // 게시글 수정
    int updateBoard(
            @Param("boardId") Long boardId,
            @Param("memberId") Long memberId,
            @Param("boardType") BoardType boardType,
            @Param("title") String title,
            @Param("content") String content
    );

    // 게시글 삭제
    int deleteBoard(
            @Param("boardId") Long boardId,
            @Param("memberId") Long memberId
    );

    //조회수 증가
    int increaseViewCount(Long boarId);

    BoardListResponse findPreviousBoard(Long boardId);

    BoardListResponse findNextBoard(Long boardId);

    // 관리자용 공지글 (최신 3개)
    List<BoardListResponse> findRecentNotices();

    // 관리자용 공지글 전체
    List<BoardListResponse> findAllNotices();

}
