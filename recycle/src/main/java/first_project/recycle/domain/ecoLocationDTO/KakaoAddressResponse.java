package first_project.recycle.domain.ecoLocationDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class KakaoAddressResponse {
    // 카카오 주소 검색 결과 목록 주소 데이터는 많기에 list로 받기
    private List<Document> documents;

    @Data
    @NoArgsConstructor
    public static class Document{

        private String x; // 경도
        private String y; // 위도
        private Address address; // 지번 주소
    }

    @Data
    @NoArgsConstructor
    public static class Address {
        // 전체 지번 주소
        @JsonProperty("address_name")
        private String addressName;
    }
}
