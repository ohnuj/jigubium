package first_project.recycle.repository;


import first_project.recycle.domain.Comment;
import first_project.recycle.dto.CommentResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 댓글 DB 처리를 담당하는 Mapper
 */
@Mapper
public interface CommentMapper {

    // 댓글 등록
    int insertComment(Comment comment);

    // 특정 게시글의 댓글 목록 조회
    List<CommentResponse> findByBoardId(Long boardId);

    // 댓글 수정
    int updateComment(
            @Param("boardId") Long boardId,
            @Param("commentId") Long commentId,
            @Param("memberId") Long memberId,
            @Param("content") String content
    );

    // 댓글 삭제
    int deleteComment(
            @Param("boardId") Long boardId,
            @Param("commentId") Long commentId,
            @Param("memberId") Long memberId
    );

    // 게시글 삭제 시 해당 게시글의 댓글 전체 삭제
    int deleteByBoardId(Long boardId);

    int countByBoardId(Long boardId);

    List<CommentResponse> findPageByBoardId(
            @Param("boardId") Long boardId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    Comment findById(
            @Param("boardId") Long boardId,
            @Param("commentId") Long commentId
    );
}
