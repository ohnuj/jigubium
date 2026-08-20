package first_project.recycle.dto;

import first_project.recycle.domain.BoardType;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 게시글 목록 조회용
 */
@Data
public class BoardListResponse {

    private Long boardId;       //게시글 번호
    private BoardType boardType;    // 게시판 타입
    private String title;           // 게시글 제목
    private String nickname;        // 작성자 닉네임
    private LocalDateTime createdAt;    // 작성일
    private LocalDateTime updateAt; // 수정일

    /**
     * 게시글 수정 여부 확인
     */
    public boolean isModified() {
        return updateAt != null
                && createdAt != null
                && !updateAt.equals(createdAt);
    }
}
