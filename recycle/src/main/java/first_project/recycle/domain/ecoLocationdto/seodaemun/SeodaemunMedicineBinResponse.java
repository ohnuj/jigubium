package first_project.recycle.domain.ecoLocationdto.seodaemun;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeodaemunMedicineBinResponse {

    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;

    private List<DataItem> data;

    @Data
    public static class DataItem {
        @JsonProperty("연번")
        private String number;

        @JsonProperty("지역")
        private String region;

        @JsonProperty("명칭")
        private String name;

        @JsonProperty("지번주소")
        private String jibunAddress;

        @JsonProperty("도로명 주소")
        private String roadAddress;

        @JsonProperty("운영시간")
        private String operatingHours;

        @JsonProperty("연락처")
        private String contact;

        @JsonProperty("데이터기준일")
        private String dataDate;

    }

}
