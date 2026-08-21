package first_project.recycle.dto;


import first_project.recycle.domain.BoardType;
import lombok.Data;

/**
 * 게시글 수정 요청
 */
@Data
public class BoardUpdateRequest {
    private BoardType boardType;
    private String title;
    private String content;
}
