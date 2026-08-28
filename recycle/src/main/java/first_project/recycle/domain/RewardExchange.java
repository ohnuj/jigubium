package first_project.recycle.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RewardExchange {
    private Long exchangeId;
    private Long memberId;
    private Long rewardId;
    private Integer pointUsed;
    private String status;
    private LocalDateTime exchangedAt;
    private LocalDateTime processedAt;

    private String rewardName;
    private String rewardNameKorean;
    private String imageUrl;

    private String name;
    private String phone;
    private String address;
    private String addressDetail;
}
