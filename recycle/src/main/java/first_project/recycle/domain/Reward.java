package first_project.recycle.domain;

import lombok.Data;

@Data
public class Reward {
    private Long rewardId;
    private String rewardName;
    private String rewardNameKorean;
    private Integer requiredPoint;
    private Integer stockQuantity;
    private String imageUrl;

}
