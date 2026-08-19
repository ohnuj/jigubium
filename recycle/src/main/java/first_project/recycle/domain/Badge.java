package first_project.recycle.domain;

import lombok.Data;

@Data
public class Badge {
    private Long badgeId;
    private String badgeName;
    private Integer requiredPoint;
    private String iconUrl;

}
