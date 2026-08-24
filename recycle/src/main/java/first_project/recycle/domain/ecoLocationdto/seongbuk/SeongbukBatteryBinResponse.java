package first_project.recycle.domain.ecoLocationdto.seongbuk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// 성북구 폐건전지·폐형광등 API 응답 DTO
@Data
public class SeongbukBatteryBinResponse {

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
        private String number;

        @JsonProperty("관리부서(동)명")
        private String adminDong;

        @JsonProperty("주소")
        private String address;

        @JsonProperty("위도")
        private String latitude;

        @JsonProperty("경도")
        private String longitude;

        @JsonProperty("위치")
        private String locationDetail;

        @JsonProperty("비고")
        private String note;
    }
}