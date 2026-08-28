package first_project.recycle.domain;

import lombok.Data;

@Data
public class RewardRequest {
    private Long requestId;
    private Long exchangeId;
    private String name;
    private String phone;
    private String address;
    private String addressDetail;


}
