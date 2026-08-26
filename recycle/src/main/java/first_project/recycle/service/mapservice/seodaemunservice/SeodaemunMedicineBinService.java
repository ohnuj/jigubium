package first_project.recycle.service.mapservice.seodaemunservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.domain.ecoLocationdto.seodaemun.SeodaemunMedicineBinResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import first_project.recycle.service.KakaoAddressService;
import first_project.recycle.mapper.EcoLocationMapper;


import java.math.BigDecimal;
import java.util.List;

@Service
public class SeodaemunMedicineBinService {

    // 공공데이터 API 인증키
    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    // 외부 API 호출용
    private final RestClient restClient = RestClient.create();

    // 주소 → 위도/경도 변환용 카카오 서비스
    private final KakaoAddressService kakaoAddressService;

    // DB 저장용 Mapper
    private final EcoLocationMapper ecoLocationMapper;

    //생성자
    public SeodaemunMedicineBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    // 서대문구 폐의약품 수거함 데이터 불러오기
    public List<EcoLocation> getMedicineBins() {

        SeodaemunMedicineBinResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.odcloud.kr")
                        .path("/api/15074855/v1/uddi:44288d18-214b-41a0-a4af-d693fd4d1bc0")
                        .queryParam("page", 1)
                        .queryParam("perPage", 100)
                        .build()
                )
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(SeodaemunMedicineBinResponse.class);

        // 응답이 없으면 빈 리스트 반환
        if (response == null || response.getData() == null) {
            return List.of();
        }
        // DataItem → ecoLocation 변환
        List<EcoLocation> locations = response
                .getData()
                .stream()
                .map(this::convertToEcoLocation)
                .toList();
        // DB에 같은 도로명주소 + 장소타입이 없을 때만 저장
        locations.forEach(location -> {
            // 위도 / 경도가 없는 데이터는 저장하지 않음
            if (
                    location.getLatitude() == null ||
                            location.getLongitude() == null
            ) {
                return;
            }
            // 같은 장소가 이미 DB에 있는지 확인
            int count =
                    ecoLocationMapper.countByRoadAddressAndLocationType(
                            location.getRoadAddress(),
                            location.getLocationType()
                    );
            // 중복이 없으면 DB 저장
            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        });
        return locations;
    }

    // 서대문구 폐의약품 데이터를 ecoLocation으로 변환
    private EcoLocation convertToEcoLocation(
            SeodaemunMedicineBinResponse.DataItem dataItem
    ) {
        EcoLocation location = new EcoLocation();

        // 장소 기본 정보
        location.setLocationName(dataItem.getName());
        location.setLocationType("폐의약품 수거함");

        // API에서 제공하는 주소 저장
        location.setRoadAddress(dataItem.getRoadAddress());
        location.setJibunAddress(dataItem.getJibunAddress());

        // 도로명주소의 괄호 부분 제거
        String searchAddress = dataItem
                .getRoadAddress()
                .replaceAll("\\([^)]*\\)", "")
                .trim();

        // 주소 → 위도/경도 변환
        KakaoAddressResponse kakaoResponse =
                kakaoAddressService.searchAddress(searchAddress);

        // 카카오 주소검색 결과가 존재할 경우
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
        }
        return location;
    }
}