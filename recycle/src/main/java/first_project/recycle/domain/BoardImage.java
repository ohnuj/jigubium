package first_project.recycle.domain;

import lombok.Data;

@Data
public class BoardImage {
    private Long image_id; // 이미지 아이디 식별자 오토
    private Long board_id; // 보드아이디 보드도메인 참조
    private String image_url; // 이미지url 경로
    private Integer sort_order; // 여러이미지 출력순서 0기가장앞
}
