package first_project.recycle.domain.ecoLocationdto.dobong;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DobongBatteryBinResponse {

    private List<Item> data;

    @Data
    public static class Item {

        @JsonProperty("장소")
        private String locationName;

        @JsonProperty("행정동")
        private String adminDong;

        @JsonProperty("주소")
        private String address;

        @JsonProperty("위도")
        private String latitude;

        @JsonProperty("경도")
        private String longitude;
    }
}
