package first_project.recycle.domain.ecoLocationDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class JongnoBatteryBinResponse {

    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;

//    폐건전지 api에서 주는 위치들을 리스트로 만들어 저장
    private List<JongnoBatteryBinDTO> data;
}
