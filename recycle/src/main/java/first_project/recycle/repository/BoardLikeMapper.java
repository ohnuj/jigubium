package first_project.recycle.repository;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardLikeMapper {

    // 좋아요 등록
    int insertLike(
            @Param("boardId") Long boardId,
            @Param("memberId") Long memberId
    );

    // 좋아요 취소
    int deleteLike(
            @Param("boardId") Long boardId,
            @Param("memberId") Long memberId
    );

    // 해당 회원이 이 게시글에 좋아요했는지 확인
    int existsLike(
            @Param("boardId") Long boardId,
            @Param("memberId") Long memberId
    );

    // 게시글 좋아요 개수
    int countByBoardId(Long boardId);
}
