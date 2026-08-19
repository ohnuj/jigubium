package first_project.recycle.dto;


import first_project.recycle.domain.BoardType;
import lombok.Data;


//등록 요청 데이터를 전달받기 위한 DTO
@Data
public class BoardCreateRequest {
    private BoardType boardType;    // 게시판 타입
    private String title;           // 게시글 제목
    private String content;         // 게시글 내용


}
