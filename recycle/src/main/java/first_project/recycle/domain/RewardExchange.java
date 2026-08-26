package first_project.recycle.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RewardExchange {
    private Long exchangeId;
    private Long memberId;
    private Long rewardId;
    private Integer pointUsed;
    private LocalDateTime exchangedAt;

    private String rewardName;
    private String rewardNameKorean;
    private String imageUrl;
}
