package first_project.recycle.domain.ecoLocationdto.gangbuk;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GangbukClothingBinResponse {

    private List<Item> data;

    @Data
    public static class Item {

        @JsonProperty("행정동")
        private String adminDong;

        @JsonProperty("도로명주소")
        private String roadAddress;

        @JsonProperty("위도")
        private String latitude;

        @JsonProperty("경도")
        private String longitude;
    }
}
