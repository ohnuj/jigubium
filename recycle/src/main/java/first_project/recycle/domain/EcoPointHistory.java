package first_project.recycle.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EcoPointHistory {
    private Long ecoPointHistoryId;
    private Long memberId;
    private Integer pointAmount;
    private Integer balanceAfter;
    private String pointType;
    private Long referenceId;
    private LocalDateTime createdAt;
}
