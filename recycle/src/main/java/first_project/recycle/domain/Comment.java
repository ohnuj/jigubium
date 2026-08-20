package first_project.recycle.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 게시글 댓글 정보를 저장하는 Domain
 */
@Data
public class Comment {

    private Long commentId; // 댓글 PK
    private Long boardId; // 게시글 FK
    private Long memberId; // 작성 회원 FK
    private String content; // 댓글 내용
    private LocalDateTime createdAt; // 작성일
    private LocalDateTime updatedAt; // 수정일
}
