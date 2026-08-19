package first_project.recycle.domain;

import lombok.Data;

@Data
public class BoardImage {
    private Long imageId; // 이미지 아이디 식별자 오토
    private Long boardId; // 보드아이디 보드도메인 참조
    private String imageUrl; // 이미지url 경로
    private Integer sortOrder; // 여러이미지 출력순서 0기가장앞
}
