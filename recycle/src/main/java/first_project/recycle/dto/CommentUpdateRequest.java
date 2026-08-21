package first_project.recycle.dto;


import lombok.Data;

/**
 * 댓글 수정 요청
 */
@Data
public class CommentUpdateRequest {

    private String content;
}
