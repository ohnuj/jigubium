package first_project.recycle.domain.ecoLocationdto.jungnang;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class JungnangBatteryBinResponse {

    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;

    private List<DataItem> data;

    // API의 개별 수거함 데이터
    @Data
    public static class DataItem {

        @JsonProperty("연번")
        private int number;

        @JsonProperty("주소")
        private String address;

        @JsonProperty("세부위치(건물명 또는 상호)")
        private String detailName;

        @JsonProperty("보유 수량")
        private int count;

        @JsonProperty("위도")
        private String latitude;

        @JsonProperty("경도")
        private String longitude;

        @JsonProperty("데이터기준일자")
        private String dataReferenceDate;
    }
}
