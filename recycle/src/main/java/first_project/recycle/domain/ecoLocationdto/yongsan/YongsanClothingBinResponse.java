package first_project.recycle.domain.ecoLocationdto.yongsan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class YongsanClothingBinResponse {

    private  int page;
    private  int perPage;
    private  int totalCount;
    private  int currentCount;

    private List<YongsanClothingBinDTO> data;

}
