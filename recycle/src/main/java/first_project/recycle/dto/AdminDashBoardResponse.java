package first_project.recycle.dto;

import lombok.Data;

@Data
public class AdminDashBoardResponse {
    private Long totalMember;
    private Long totalBoard;
    private Long todayMember;
    private Long todayBoard;
    private Long uncheckedSuggestion;
}
