package first_project.recycle.service.mapservice.seongbukservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.seongbuk.SeongbukBatteryBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

// 위도 경도를 제공해서 카카오 주소 검색 api 사용 x

@Service
public class SeongbukBatteryBinService {
    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    private final RestClient restClient;
    private final EcoLocationMapper ecoLocationMapper;

    public SeongbukBatteryBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.restClient = RestClient.create();
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 성북구 공공데이터 API 호출
     * 2. API 응답을 ecoLocation 목록으로 변환
     * 3. 주소와 좌표가 정상적인 데이터만 선별
     * 4. 중복되지 않은 데이터만 DB에 저장
     */
    public List<EcoLocation> getBatteryBins() {

        // 1. 성북구 폐건전지·폐형광등 API 주소
        String url =
                "https://api.odcloud.kr/api/15038083/v1/"
                        + "uddi:302eeaec-65b4-469a-b6d6-c9de7080b1d4"
                        + "?page=1&perPage=1000";

        // 2. API를 호출하고 JSON 응답을 DTO로 변환
        SeongbukBatteryBinResponse response =
                restClient
                        .get()
                        .uri(url)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(SeongbukBatteryBinResponse.class);

        // API 응답 또는 data가 없으면 빈 목록 반환
        if (response == null || response.getData() == null) {
            return List.of();
        }

        // 3. DataItem을 ecoLocation으로 변환
        List<EcoLocation> locations =
                response.getData()
                        .stream()
                        .map(this::convertToEcoLocation)
                        .toList();

        // 4. 변환된 데이터를 검사하고 DB에 저장
        for (EcoLocation location : locations) {

            // 주소 또는 좌표가 없으면 저장하지 않음
            if (
                    location.getRoadAddress() == null ||
                            location.getRoadAddress().isBlank() ||
                            location.getLatitude() == null ||
                            location.getLongitude() == null
            ) {
                continue;
            }

            // 같은 주소와 장소 유형이 있는지 확인
            int count =
                    ecoLocationMapper
                            .countByRoadAddressAndLocationType(
                                    location.getRoadAddress(),
                                    location.getLocationType()
                            );

            // 중복되지 않은 데이터만 저장
            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        }

        // 변환된 전체 목록을 JSON으로 반환
        return locations;
    }

    // API 데이터를 ecoLocation으로 변환
    private EcoLocation convertToEcoLocation(
            SeongbukBatteryBinResponse.DataItem dataItem
    ) {
        EcoLocation location = new EcoLocation();

        /*
         * 장소명 우선순위
         * 1. 위치
         * 2. 비고
         * 3. 주소
         * 4. 기본 이름
         */
        String locationName = dataItem.getLocationDetail();

        if (locationName == null || locationName.isBlank()) {
            locationName = dataItem.getNote();
        }

        if (locationName == null || locationName.isBlank()) {
            locationName = dataItem.getAddress();
        }

        if (locationName == null || locationName.isBlank()) {
            locationName = "폐건전지·폐형광등 수거함";
        }

        location.setLocationName(locationName);

        // 지도 필터에 사용하는 장소 유형
        location.setLocationType(
                "폐건전지·폐형광등 수거함"
        );

        location.setAdminDong(dataItem.getAdminDong());
        location.setRoadAddress(dataItem.getAddress());

        // API가 제공한 좌표를 BigDecimal로 변환
        location.setLatitude(
                parseCoordinate(dataItem.getLatitude())
        );

        location.setLongitude(
                parseCoordinate(dataItem.getLongitude())
        );

        return location;
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

