package first_project.recycle.domain.ecoLocationdto.yongsan;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class YongsanMedicineBinResponse {
    private int page;
    private int perPage;
    private int totalCount;
    private int currentCount;

    private List<YongsanMedicineBinDTO> data;

}


/**
 * 용산구 폐의약품 API
 *         ↓
 * YongsanMedicineBinDto
 *         ↓
 * ecoLocation 변환
 *         ↓
 * locationType = "폐의약품"
 *         ↓
 * eco_location DB 저장
 *         ↓
 * /api/medicine-bins
 *         ↓
 * eco-map에서 폐의약품 버튼
 *         ↓
 * 폐의약품 마커만 표시
 */