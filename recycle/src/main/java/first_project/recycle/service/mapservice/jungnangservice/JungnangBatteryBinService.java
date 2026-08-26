package first_project.recycle.service.mapservice.jungnangservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.jungnang.JungnangBatteryBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

// api가 좌표를 제공하므로 카카오 api로 주소 검색 사용 x
@Service
public class JungnangBatteryBinService {
    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    private final RestClient restClient;
    private final EcoLocationMapper ecoLocationMapper;

    public JungnangBatteryBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.restClient = RestClient.create();
        this.ecoLocationMapper = ecoLocationMapper;
    }
    /**
     * 1. 중랑구 공공데이터 API 호출
     * 2. API 데이터를 ecoLocation으로 변환
     * 3. 주소와 좌표가 정상적인 데이터 선별
     * 4. 중복되지 않은 데이터만 DB에 저장
     */
    public List<EcoLocation> getBatteryBins() {

        // 1. 최신 2026년 중랑구 API 호출 주소
        String url =
                "https://api.odcloud.kr/api/15038000/v1/"
                        + "uddi:54f6f853-7544-40ef-a7ae-cede3c963665"
                        + "?page=1&perPage=1000";

        // 2. JSON 응답을 DTO로 변환
        JungnangBatteryBinResponse response =
                restClient
                        .get()
                        .uri(url)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(JungnangBatteryBinResponse.class);

        // API 응답이나 data가 없으면 빈 목록 반환
        if (response == null || response.getData() == null) {
            return List.of();
        }

        // 3. DataItem을 ecoLocation으로 변환
        List<EcoLocation> locations =
                response.getData()
                        .stream()
                        .map(this::convertToEcoLocation)
                        .toList();

        // 4. 정상적인 데이터만 DB에 저장
        for (EcoLocation location : locations) {

            if (
                    location.getRoadAddress() == null ||
                            location.getRoadAddress().isBlank() ||
                            location.getLatitude() == null ||
                            location.getLongitude() == null
            ) {
                continue;
            }

            // 같은 주소와 장소 유형인지 확인
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

        return locations;
    }

    // API 데이터를 ecoLocation으로 변환
    private EcoLocation convertToEcoLocation(
            JungnangBatteryBinResponse.DataItem dataItem
    ) {
        EcoLocation location = new EcoLocation();

        // 세부위치를 장소명으로 사용
        String locationName = dataItem.getDetailName();

        // 세부위치가 없으면 주소 사용
        if (locationName == null || locationName.isBlank()) {
            locationName = dataItem.getAddress();
        }

        // 주소도 없으면 기본 이름 사용
        if (locationName == null || locationName.isBlank()) {
            locationName = "폐건전지·폐형광등 수거함";
        }

        location.setLocationName(locationName);

        location.setLocationType(
                "폐건전지·폐형광등 수거함"
        );

        location.setRoadAddress(dataItem.getAddress());

        // API 좌표를 BigDecimal로 변환
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
