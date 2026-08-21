package first_project.recycle.domain.ecoLocationdto.yongsan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class YongsanMedicineBinDTO {
    @JsonProperty("순번")
    private Integer number;

    @JsonProperty("명칭")
    private String name;

    @JsonProperty("수거함위치")
    private String binLocation;

    @JsonProperty("도로명주소")
    private String roadAddress;

    @JsonProperty("관리기관")
    private String managementAgency;

    @JsonProperty("관리기관전화번호")
    private String managementPhone;
}
