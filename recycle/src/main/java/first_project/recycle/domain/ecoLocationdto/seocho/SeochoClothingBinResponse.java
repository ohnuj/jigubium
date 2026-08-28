package first_project.recycle.domain.ecoLocationdto.seocho;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeochoClothingBinResponse {

    private List<Item> data;

    @Data
    public static class Item {

        @JsonProperty("행정동")
        private String adminDong;

        @JsonProperty("설치장소명")
        private String locationName;

        @JsonProperty("소재지도로명주소")
        private String roadAddress;

        @JsonProperty("소재지지번주소")
        private String jibunAddress;

        @JsonProperty("위도")
        private String latitude;

        @JsonProperty("경도")
        private String longitude;
    }
}
