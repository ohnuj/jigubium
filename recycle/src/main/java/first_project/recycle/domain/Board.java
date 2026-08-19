package first_project.recycle.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Board {
   private Long board_id; //  보드아이디 식별자 오토
   private Long member_id; // 멤버아이디 멤버테이블참조키
   private String board_type; // 보드타입 설정 건의,정보,자유
   private String title; // 글작성제목
   private String content; // 글작성내용
   private LocalDateTime created_at; // 작성일자
   private LocalDateTime updated_at; // 수정일자
}
