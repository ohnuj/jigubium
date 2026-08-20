package first_project.recycle.domain.ecoLocationdto.yongsan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class YongsanClothingBinDTO {

    @JsonProperty("연 번")
    private Integer number;
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
}
// 용산구 의료수거함 api는 위도, 경도를 제공해서 카카오 주소검색 안거쳐도 됨