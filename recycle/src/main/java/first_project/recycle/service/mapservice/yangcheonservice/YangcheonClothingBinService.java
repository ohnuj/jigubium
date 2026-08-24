package first_project.recycle.service.mapservice.yangcheonservice;

import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationdto.yangcheon.YangcheonClothingBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

// api에 위도 경도 있으므로 카카오 주소 검색 사용 x
@Service
public class YangcheonClothingBinService {
    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    private final RestClient restClient;
    private final EcoLocationMapper ecoLocationMapper;

    public YangcheonClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.restClient = RestClient.create();
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 양천구 의류수거함 API 호출
     * 2. API 데이터를 ecoLocation으로 변환
     * 3. 주소와 좌표가 정상적인 데이터 선별
     * 4. 중복되지 않은 데이터만 DB에 저장
     */
    public List<ecoLocation> getClothingBins() {

        // 1. 최신 양천구 의류수거함 API
        String url =
                "https://api.odcloud.kr/api/15105196/v1/"
                        + "uddi:e31cb0fd-ed8a-4d19-8b04-d99b6358fa28"
                        + "?page=1&perPage=1000";

        YangcheonClothingBinResponse response =
                restClient
                        .get()
                        .uri(url)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(YangcheonClothingBinResponse.class);

        // API 응답이 없으면 빈 목록 반환
        if (response == null || response.getData() == null) {
            return List.of();
        }

        // 2. DataItem을 ecoLocation으로 변환
        List<ecoLocation> locations =
                response.getData()
                        .stream()
                        .map(this::convertToEcoLocation)
                        .toList();

        // 3. 정상적인 데이터만 DB에 저장
        for (ecoLocation location : locations) {

            if (
                    location.getRoadAddress() == null ||
                            location.getRoadAddress().isBlank() ||
                            location.getLatitude() == null ||
                            location.getLongitude() == null
            ) {
                continue;
            }

            // 4. 같은 주소와 장소 유형이 있는지 확인
            int count =
                    ecoLocationMapper
                            .countByRoadAddressAndLocationType(
                                    location.getRoadAddress(),
                                    location.getLocationType()
                            );

            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        }

        return locations;
    }

    // API 데이터를 ecoLocation으로 변환
    private ecoLocation convertToEcoLocation(
            YangcheonClothingBinResponse.DataItem dataItem
    ) {
        ecoLocation location = new ecoLocation();

        location.setLocationName("의류수거함");
        location.setLocationType("의류수거함");

        location.setAdminDong(
                normalizeText(dataItem.getAdminDong())
        );

        String roadAddress =
                normalizeText(dataItem.getRoadAddress());

        String jibunAddress =
                normalizeText(dataItem.getJibunAddress());

        // 도로명주소가 없으면 지번주소를 대표 주소로 사용
        String mainAddress = roadAddress;

        if (mainAddress == null || mainAddress.isBlank()) {
            mainAddress = jibunAddress;
        }

        location.setRoadAddress(mainAddress);
        location.setJibunAddress(jibunAddress);

        // API가 제공하는 좌표를 BigDecimal로 변환
        location.setLatitude(
                parseCoordinate(dataItem.getLatitude())
        );

        location.setLongitude(
                parseCoordinate(dataItem.getLongitude())
        );

        return location;
    }

    // 문자열의 불필요한 공백 정리
    private String normalizeText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    // 문자열 좌표를 BigDecimal로 변환
    private BigDecimal parseCoordinate(String coordinate) {

        if (coordinate == null || coordinate.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(coordinate.trim());

        } catch (NumberFormatException e) {
            return null;
        }
    }
}
