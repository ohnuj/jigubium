package first_project.recycle.domain;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 게시글 좋아요
 */
@Data
public class BoardLike {

    private Long likeId;
    private Long boardId;
    private Long memberId;
    private LocalDateTime createdAt;
}
