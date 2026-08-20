package first_project.recycle.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 댓글 조회용
 */
@Data
public class CommentResponse {
    private Long commentId;
    private Long boardId;
    private Long memberId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


// 댓글이 수정됐는지 확인
public boolean isModified() {
    return updatedAt != null
            && createdAt != null
            && !updatedAt.equals(createdAt);
}
}