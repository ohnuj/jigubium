package first_project.recycle.service;


import first_project.recycle.domain.Comment;
import first_project.recycle.dto.CommentCreateRequest;
import first_project.recycle.dto.CommentResponse;
import first_project.recycle.dto.CommentUpdateRequest;
import first_project.recycle.repository.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 댓글 관련 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    /**
     * 특정 게시글의 댓글 목록 조회
     */

    public List<CommentResponse> getComments(Long boardId) {
        return commentMapper.findByBoardId(boardId);
    }

    /**
     * 댓글 등록
     */
    @Transactional
    public void createComment(
            Long boardId,
            Long memberId,
            CommentCreateRequest request){

        Comment comment = new Comment();

        comment.setBoardId(boardId);
        comment.setMemberId(memberId);
        comment.setContent(request.getContent());

        commentMapper.insertComment(comment);
    }

    /**
     * 댓글 수정
     * 작성자 본인의 댓글만 수정 가능
     */
    @Transactional
    public boolean updateComment(
            Long commentId,
            Long memberId,
            CommentUpdateRequest request) {

        int result = commentMapper.updateComment(
                commentId,
                memberId,
                request.getContent()
        );

        return result > 0;
    }


    /**
     * 댓글 삭제
     * 작성자 본인의 댓글만 삭제 가능
     */
    @Transactional
    public boolean deleteComment(
            Long commentId,
            Long memberId) {

        int result =
                commentMapper.deleteComment(commentId, memberId);

        return result > 0;
    }

    /**
     * 게시글 삭제 시 해당 게시글의 댓글 전체 삭제
     */
    @Transactional
    public void deleteByBoardId(Long boardId) {
        commentMapper.deleteByBoardId(boardId);
    }

}
