package first_project.recycle.domain.ecoLocationdto.dongjak;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DongjakMedicineBinResponse {

    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;
    private List<DataItem> data;

    @Data
    public static class DataItem {

        @JsonProperty("약국명칭")
        private String pharmacyName;

        @JsonProperty("약국전화번호")
        private String phoneNumber;

        @JsonProperty("약국소재지")
        private String address;

        @JsonProperty("데이터기준일자")
        private String dataReferenceDate;
    }
}
