package first_project.recycle.domain.ecoLocationdto.dongjak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DongjakBatteryBinResponse {

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

        @JsonProperty("구분")
        private String category;

        @JsonProperty("지번주소")
        private String jibunAddress;

        @JsonProperty("도로명주소")
        private String roadAddress;

        @JsonProperty("건 물 명")
        private String buildingName;

        @JsonProperty("개수")
        private int count;
    }
}
