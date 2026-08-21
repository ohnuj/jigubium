package first_project.recycle.domain;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GameHistory {
    private Long gameHistoryId;
    private Long memberId;
    private int score;
    private int totalCount;
    private int correctCount;
    private LocalDateTime playedAt;
}
