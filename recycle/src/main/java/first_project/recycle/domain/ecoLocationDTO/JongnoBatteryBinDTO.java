package first_project.recycle.domain.ecoLocationDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JongnoBatteryBinDTO {
    // api속 한글 필드명을 영문으로 바꿔주기
    @JsonProperty("연번")
    private String number;
    @JsonProperty("행정동")
    private String adminDong;
    @JsonProperty("위치")
    private String location;
    @JsonProperty("설치장소")
    private String locationName;
    @JsonProperty("데이터기준일자")
    private String dataDate;
}
