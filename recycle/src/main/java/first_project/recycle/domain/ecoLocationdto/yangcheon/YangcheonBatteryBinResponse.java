package first_project.recycle.domain.ecoLocationdto.yangcheon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class YangcheonBatteryBinResponse {
    // API 페이지 정보
    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;

    // 폐건전지·폐형광등 수거함 데이터 목록
    private List<DataItem> data;

    @Data
    public static class DataItem {

        @JsonProperty("번호")
        private int number;

        @JsonProperty("행정동")
        private String adminDong;

        @JsonProperty("수거함 설치위치")
        private String installationLocation;

        @JsonProperty("수거함종류")
        private String binType;

        @JsonProperty("데이터기준일자")
        private String dataReferenceDate;
    }
}
