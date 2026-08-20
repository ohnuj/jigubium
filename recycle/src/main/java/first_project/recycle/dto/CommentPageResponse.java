package first_project.recycle.dto;

import first_project.recycle.domain.Paging;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 댓글 목록과 페이징 정보
 */
@Data
@AllArgsConstructor
public class CommentPageResponse {

    private List<CommentResponse> comments;
    private Paging paging;
}
