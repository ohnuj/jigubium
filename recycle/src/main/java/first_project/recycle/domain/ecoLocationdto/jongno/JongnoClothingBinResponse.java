package first_project.recycle.domain.ecoLocationdto.jongno;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JongnoClothingBinResponse {
    private int page;
    private  int perPage;
    private  int totalCount;
    private  int currentCount;

    private List<JongnoClothingBinDto> data;
}
