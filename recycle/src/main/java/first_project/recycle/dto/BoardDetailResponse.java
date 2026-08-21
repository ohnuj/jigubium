package first_project.recycle.dto;


import first_project.recycle.domain.BoardImage;
import first_project.recycle.domain.BoardType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 조회용
 */
@Data
public class BoardDetailResponse {

    private Long boardId;
    private Long memberId;
    private BoardType boardType;
    private String title;
    private String content;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Integer likeCount;
    private boolean liked;

    private List<BoardImage> images;

    public boolean isModified() {
        return updatedAt != null
                && createdAt != null
                && !createdAt.equals(updatedAt);
    }
}
