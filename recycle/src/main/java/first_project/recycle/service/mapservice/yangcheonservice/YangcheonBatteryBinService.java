package first_project.recycle.service.mapservice.yangcheonservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.domain.ecoLocationdto.yangcheon.YangcheonBatteryBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//api에서 좌표를 안주기때문에 카카오 주소 검색 사용
@Service
public class YangcheonBatteryBinService {
    /*
     * 설치위치에서 지번주소 부분을 추출
     * 예: 서울특별시 양천구 목1동 404 푸르지오아파트
     * → 서울특별시 양천구 목1동 404
     */
    private static final Pattern JIBUN_ADDRESS_PATTERN =
            Pattern.compile(
                    "^(서울특별시\\s+양천구\\s+\\S+동\\s+\\d+(?:-\\d+)?)"
            );

    // 도로명주소 형식 데이터가 있을 경우 사용
    private static final Pattern ROAD_ADDRESS_PATTERN =
            Pattern.compile(
                    "^(서울특별시\\s+양천구\\s+.+?(?:로|길)\\s*\\d+(?:-\\d+)?)"
            );

    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    private final RestClient restClient;
    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public YangcheonBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.restClient = RestClient.create();
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 양천구 공공데이터 API 호출
     * 2. 응답 데이터를 ecoLocation으로 변환
     * 3. 설치위치를 카카오 API로 검색
     * 4. 좌표가 정상인 데이터만 DB에 저장
     */
    public List<EcoLocation> getBatteryBins() {

        // 1. 최신 양천구 폐건전지·폐형광등 API
        String url =
                "https://api.odcloud.kr/api/15038109/v1/"
                        + "uddi:5176aa33-e48f-46fc-8e01-cb8e0d2984fb"
                        + "?page=1&perPage=1000";

        YangcheonBatteryBinResponse response =
                restClient
                        .get()
                        .uri(url)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(YangcheonBatteryBinResponse.class);

        // API 응답이 없으면 빈 목록 반환
        if (response == null || response.getData() == null) {
            return List.of();
        }

        // 2. DataItem을 ecoLocation으로 변환
        List<EcoLocation> locations =
                response.getData()
                        .stream()
                        .map(this::convertToEcoLocation)
                        .toList();

        // 3. 정상 데이터만 중복 확인 후 DB 저장
        for (EcoLocation location : locations) {

            if (
                    location.getRoadAddress() == null ||
                            location.getRoadAddress().isBlank() ||
                            location.getLatitude() == null ||
                            location.getLongitude() == null
            ) {
                continue;
            }

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
    private EcoLocation convertToEcoLocation(
            YangcheonBatteryBinResponse.DataItem dataItem
    ) {
        EcoLocation location = new EcoLocation();

        String installationLocation =
                normalizeText(dataItem.getInstallationLocation());

        String searchAddress =
                extractSearchAddress(installationLocation);

        /*
         * 장소명 우선순위
         * 1. 주소 뒤의 건물명 또는 상세 위치
         * 2. 전체 설치위치
         * 3. 수거함 종류
         * 4. 기본 이름
         */
        String locationName =
                extractLocationName(
                        installationLocation,
                        searchAddress
                );

        if (locationName == null || locationName.isBlank()) {
            locationName = dataItem.getBinType();
        }

        if (locationName == null || locationName.isBlank()) {
            locationName = "폐건전지·폐형광등 수거함";
        }

        location.setLocationName(locationName);
        location.setLocationType(
                "폐건전지·폐형광등 수거함"
        );

        location.setAdminDong(dataItem.getAdminDong());

        // 단일 설치위치를 대표 주소로 저장
        location.setRoadAddress(installationLocation);

        if (
                searchAddress == null ||
                        searchAddress.isBlank()
        ) {
            return location;
        }

        // 정리한 주소를 카카오 API로 검색
        KakaoAddressResponse kakaoResponse =
                searchAddress(searchAddress);

        // 정리한 주소 검색 실패 시 원본으로 다시 검색
        if (
                !hasSearchResult(kakaoResponse) &&
                        !searchAddress.equals(installationLocation)
        ) {
            kakaoResponse =
                    searchAddress(installationLocation);
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

    // 주소의 불필요한 공백 정리
    private String normalizeText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    // 설치위치에서 카카오 검색에 사용할 주소 추출
    private String extractSearchAddress(String location) {

        if (location == null || location.isBlank()) {
            return null;
        }

        Matcher jibunMatcher =
                JIBUN_ADDRESS_PATTERN.matcher(location);

        if (jibunMatcher.find()) {
            return jibunMatcher.group(1);
        }

        Matcher roadMatcher =
                ROAD_ADDRESS_PATTERN.matcher(location);

        if (roadMatcher.find()) {
            return roadMatcher.group(1);
        }

        return location;
    }

    // 주소 뒤에 붙은 건물명 또는 상세 위치 추출
    private String extractLocationName(
            String installationLocation,
            String searchAddress
    ) {
        if (
                installationLocation == null ||
                        installationLocation.isBlank()
        ) {
            return null;
        }

        if (
                searchAddress != null &&
                        installationLocation.startsWith(searchAddress)
        ) {
            String detailName =
                    installationLocation
                            .substring(searchAddress.length())
                            .trim();

            if (!detailName.isBlank()) {
                return detailName;
            }
        }

        return installationLocation;
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
