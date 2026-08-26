package first_project.recycle.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EcoLocation {
    private Long locationId; // 장소 id
    private String locationName;  // 장소명
    private String locationType;  // 장소타입(ex. 의류수거함, 페건전지)
    private String adminDong;  // 행정동
    private String roadAddress;  // 도로명주소
    private String jibunAddress; // 지번
    private BigDecimal latitude;  // 위도
    private BigDecimal longitude;  // 경도
}
