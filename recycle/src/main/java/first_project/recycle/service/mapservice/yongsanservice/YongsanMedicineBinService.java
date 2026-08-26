package first_project.recycle.service.mapservice.yongsanservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.domain.ecoLocationdto.yongsan.YongsanMedicineBinDTO;
import first_project.recycle.domain.ecoLocationdto.yongsan.YongsanMedicineBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;


@Service
public class YongsanMedicineBinService {
    // 공공데이터 API 인증키
    @Value("${public-data.service-key}")
    private String serviceKey;

    // 용산구 폐의약품 수거함 API 주소
    @Value("${public-data.yongsan-medicine-bin.url}")
    private String apiUrl;

    // 외부 API 호출용
    private final RestClient restClient = RestClient.create();

    // 주소 → 위도/경도 변환용 카카오 서비스
    private final KakaoAddressService kakaoAddressService;


    // DB 저장용 Mapper
    private final EcoLocationMapper ecoLocationMapper;

    // 생성자 주입
    public YongsanMedicineBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }
    public List<EcoLocation> getMedicineBins() {

        String url =
                apiUrl
                        + "?page=1"
                        + "&perPage=1000"
                        + "&returnType=JSON";

        // 용산구 폐의약품 수거함 공공데이터 API 호출
        YongsanMedicineBinResponse response = restClient.get()
                .uri(url)
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(YongsanMedicineBinResponse.class);

        // 응답이 비어 있으면 빈 리스트 반환
        if (response == null || response.getData() == null) {
            return List.of();
        }

        List<EcoLocation> locations = response.getData().stream()
                .map(this::convertToEcoLocation)
                .toList();

// DB에 같은 도로명주소 + 장소타입이 없을 때만 저장
        locations.forEach(location -> {

            System.out.println(
                    "저장 직전 타입: " + location.getLocationType()
            );

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

            System.out.println("count: " + count);

            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        });
        return locations;
    }
    // 용산구 폐의약품 DTO를 ecoLocation으로 변환
    private EcoLocation convertToEcoLocation(
            YongsanMedicineBinDTO dto
    ) {

        EcoLocation location = new EcoLocation();

        // 폐의약품 수거 위치 기본 정보
        location.setLocationName("폐의약품 수거함");
        location.setLocationType("폐의약품 수거함");

        // API에서 제공하는 도로명주소 저장
        location.setRoadAddress(dto.getRoadAddress());

        // 도로명주소를 카카오 주소 검색 API에 전달
        KakaoAddressResponse kakaoResponse =
                kakaoAddressService.searchAddress(dto.getRoadAddress());

        // 카카오 주소 검색 결과가 존재하는 경우
        if (kakaoResponse != null
                && kakaoResponse.getDocuments() != null
                && !kakaoResponse.getDocuments().isEmpty()) {

            KakaoAddressResponse.Document document =
                    kakaoResponse.getDocuments().get(0);

            // 위도
            location.setLatitude(
                    new BigDecimal(document.getY())
            );

            // 경도
            location.setLongitude(
                    new BigDecimal(document.getX())
            );

            // 지번주소
            if (document.getAddress() != null) {
                location.setJibunAddress(
                        document.getAddress().getAddressName()
                );
            }
        }

        return location;
    }
}

/**
 * 용산구 폐의약품 API
 * → YongsanMedicineBinDto
 * → ecoLocation 변환
 * → 카카오 주소검색으로 좌표 채움
 * → roadAddress + locationType 중복 확인
 * → DB 저장
 */
