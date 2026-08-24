package first_project.recycle.domain.ecoLocationdto.yangcheon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class YangcheonClothingBinResponse {
    // API 페이지 정보
    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;

    // 양천구 의류수거함 데이터 목록
    private List<DataItem> data;

    @Data
    public static class DataItem {

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
        private String dataReferenceDate;
    }
}
