package first_project.recycle.domain.ecoLocationDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JongnoClothingBinDto {
    @JsonProperty("행정동")
    private String adminDong;

    @JsonProperty("도로명주소")
    private String roadAddress;

    @JsonProperty("지번주소")
    private String jibunAddress;

    @JsonProperty("위도")
    private String latitude;

    @JsonProperty("경도")
    private String longitude;

    @JsonProperty("데이터기준일자")
    private String dataDate;
}
