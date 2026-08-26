package first_project.recycle.service.mapservice.yongsanservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.yongsan.YongsanClothingBinDTO;
import first_project.recycle.domain.ecoLocationdto.yongsan.YongsanClothingBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Service
public class YongsanClothingBinService {
    //공공데이터 api 인증키넣기
    @Value("${public-data.service-key}")
    private String serviceKey;

    // 용산구 의류수거함 API 주소
    @Value("${public-data.yongsan-clothing-bin.url}")
    private String apiUrl;

    // 외부 API 호출용
    private final RestClient restClient = RestClient.create();

    // eco_location DB 접근용 Mapper
    private final EcoLocationMapper ecoLocationMapper;

    // 생성자 주입
    public YongsanClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }


    /**
     * 용산구 API 호출
     * → YongsanClothingBinDto
     * → ecoLocation 변환
     * → 도로명주소 + locationType 중복 체크
     * → 없으면 eco_location 테이블에 INSERT
     * → List<ecoLocation> 반환
     *
     */

    public List<EcoLocation> getClothingBins() {

        String url =
                apiUrl
                        + "?page=1"
                        + "&perPage=1000"
                        + "&returnType=JSON";

        // 용산구 의류수거함 공공데이터 API 호출
        YongsanClothingBinResponse response = restClient.get()
                .uri(url)
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(YongsanClothingBinResponse.class);

        // 응답이 비어 있으면 빈 리스트 반환
        if (response == null || response.getData() == null) {
            return List.of();
        }

        List<EcoLocation> locations = response.getData().stream()
                .map(this::convertToEcoLocation)
                .toList();

// DB에 같은 도로명주소 + 장소타입이 없을 때만 저장
        locations.forEach(location -> {

            // 좌표가 없는 데이터는 DB 저장하지 않음
            if (
                    location.getLatitude() == null ||
                            location.getLongitude() == null
            ) {
                return;
            }

            int count =
                    ecoLocationMapper.countByRoadAddressAndLocationType(
                            location.getRoadAddress(),
                            location.getLocationType()
                    );

            // 중복되지 않으면 저장
            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        });

        return locations;
    }
    // 용산구 API DTO를 우리 프로젝트의 ecoLocation 구조로 변환
    private EcoLocation convertToEcoLocation(
            YongsanClothingBinDTO dto
    ) {

        EcoLocation location = new EcoLocation();

        location.setLocationName("의류수거함");
        location.setLocationType("의류수거함");

        location.setAdminDong(dto.getAdminDong());
        location.setRoadAddress(dto.getRoadAddress());
        location.setJibunAddress(dto.getJibunAddress());

        // API에서 위도/경도를 String으로 주기 때문에 BigDecimal로 변환
        location.setLatitude(
                new BigDecimal(dto.getLatitude())
        );

        location.setLongitude(
                new BigDecimal(dto.getLongitude())
        );

        return location;
    }


}
