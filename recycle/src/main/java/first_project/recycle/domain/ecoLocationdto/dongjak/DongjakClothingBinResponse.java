package first_project.recycle.domain.ecoLocationdto.dongjak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DongjakClothingBinResponse {
    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;
    private List<DataItem> data;

    @Data
    public static class DataItem {
        @JsonProperty("연번")
        private int number;
        @JsonProperty("행정동")
        private String adminDong;
        @JsonProperty("주소")
        private String address;
        @JsonProperty("위도")
        private String latitude;
        @JsonProperty("경도")
        private String longitude;
        @JsonProperty("데이터기준일자")
        private String dataReferenceDate;
    }
}
