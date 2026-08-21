package first_project.recycle.domain.ecoLocationdto.seodaemun;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SeodaemunClothingBinResponse {

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

        @JsonProperty("관리단체")
        private String managementOrganization;

        @JsonProperty("행정동")
        private String adminDong;

        @JsonProperty("설치장소(도로명)")
        private String roadAddress;

        @JsonProperty("위도")
        private String latitude;

        @JsonProperty("경도")
        private String longitude;
    }
}
