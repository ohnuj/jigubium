package first_project.recycle.domain.ecoLocationdto.yangcheon;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class YangcheonMedicineBinResponse {
    // API 페이지 정보
    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;

    // 양천구 폐의약품 수거함 데이터 목록
    private List<DataItem> data;

    @Data
    public static class DataItem {

        @JsonProperty("연번")
        private String number;

        @JsonProperty("수거함 위치명")
        private String locationName;

        @JsonProperty("주소")
        private String address;

        @JsonProperty("전화번호")
        private String phoneNumber;

        @JsonProperty("데이터기준일자")
        private String dataReferenceDate;
    }
}
