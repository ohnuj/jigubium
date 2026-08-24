package first_project.recycle.domain.ecoLocationdto.songpa;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SongpaMedicineBinResponse {

    // API 페이지 정보
    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;
    private int matchCount;

    // 송파구 폐의약품 수거 참여 약국 목록
    private List<DataItem> data;

    @Data
    public static class DataItem {

        @JsonProperty("약국명칭")
        private String pharmacyName;

        @JsonProperty("약국소재지(도로명)")
        private String roadAddress;

        @JsonProperty("데이터기준일자")
        private String dataReferenceDate;
    }
}
