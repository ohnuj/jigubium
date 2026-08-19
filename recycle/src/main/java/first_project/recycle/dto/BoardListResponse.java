package first_project.recycle.dto;

import first_project.recycle.domain.BoardType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardListResponse {

    private Long boardId;       //게시글 번호
    private BoardType boardType;    // 게시판 타입
    private String title;           // 게시글 제목
    private String nickname;        // 작성자 닉네임
    private LocalDateTime createdAt;    // 작성일
}
