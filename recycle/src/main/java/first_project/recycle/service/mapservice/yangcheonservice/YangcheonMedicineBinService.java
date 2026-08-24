package first_project.recycle.service.mapservice.yangcheonservice;
import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.domain.ecoLocationdto.yangcheon.YangcheonMedicineBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

// api에 좌표가 없기때문에 카카오 주소 검색 사용
@Service
public class YangcheonMedicineBinService {
    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    private final RestClient restClient;
    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public YangcheonMedicineBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.restClient = RestClient.create();
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 양천구 폐의약품 수거함 API 호출
     * 2. API 데이터를 ecoLocation으로 변환
     * 3. 주소를 카카오 API로 검색하여 좌표 변환
     * 4. 중복되지 않은 정상 데이터만 DB에 저장
     */
    public List<ecoLocation> getMedicineBins() {

        // 1. 양천구 폐의약품 수거함 API
        String url =
                "https://api.odcloud.kr/api/15074902/v1/"
                        + "uddi:25d1bdd6-e0de-4cf2-b7a9-a06b6bcae57b"
                        + "?page=1&perPage=1000";

        YangcheonMedicineBinResponse response =
                restClient
                        .get()
                        .uri(url)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(YangcheonMedicineBinResponse.class);

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
            YangcheonMedicineBinResponse.DataItem dataItem
    ) {
        ecoLocation location = new ecoLocation();

        // 수거함 위치명을 장소명으로 사용
        String locationName =
                normalizeText(dataItem.getLocationName());

        if (locationName == null || locationName.isBlank()) {
            locationName = "폐의약품 수거함";
        }

        location.setLocationName(locationName);
        location.setLocationType("폐의약품 수거함");

        // API가 제공하는 원본 주소 저장
        String originalAddress =
                normalizeText(dataItem.getAddress());

        location.setRoadAddress(originalAddress);

        if (
                originalAddress == null ||
                        originalAddress.isBlank()
        ) {
            return location;
        }

        // 상세 주소를 제거한 주소로 카카오 API 검색
        String searchAddress =
                cleanAddress(originalAddress);

        KakaoAddressResponse kakaoResponse =
                searchAddress(searchAddress);

        // 정리한 주소 검색 실패 시 원본 주소로 다시 검색
        if (
                !hasSearchResult(kakaoResponse) &&
                        !searchAddress.equals(originalAddress)
        ) {
            kakaoResponse =
                    searchAddress(originalAddress);
        }

        if (hasSearchResult(kakaoResponse)) {

            KakaoAddressResponse.Document document =
                    kakaoResponse.getDocuments().get(0);

            location.setLatitude(
                    parseCoordinate(document.getY())
            );

            location.setLongitude(
                    parseCoordinate(document.getX())
            );
        }

        return location;
    }

    // 문자열의 불필요한 공백 정리
    private String normalizeText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    // 쉼표 이후와 주소 끝의 괄호 내용 제거
    private String cleanAddress(String address) {

        String cleanedAddress = address.trim();

        int commaIndex = cleanedAddress.indexOf(",");

        if (commaIndex >= 0) {
            cleanedAddress =
                    cleanedAddress.substring(0, commaIndex);
        }

        cleanedAddress =
                cleanedAddress
                        .replaceAll(
                                "\\s*\\([^)]*\\)\\s*$",
                                ""
                        )
                        .trim();

        return cleanedAddress;
    }

    // 값이 있는 주소만 카카오 API로 검색
    private KakaoAddressResponse searchAddress(String address) {

        if (address == null || address.isBlank()) {
            return null;
        }

        return kakaoAddressService.searchAddress(address);
    }

    // 카카오 주소 검색 결과 확인
    private boolean hasSearchResult(
            KakaoAddressResponse kakaoResponse
    ) {
        return kakaoResponse != null &&
                kakaoResponse.getDocuments() != null &&
                !kakaoResponse.getDocuments().isEmpty();
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
