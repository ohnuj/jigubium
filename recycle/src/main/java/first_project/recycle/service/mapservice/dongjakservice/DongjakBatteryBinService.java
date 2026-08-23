package first_project.recycle.service.mapservice.dongjakservice;

import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.domain.ecoLocationdto.dongjak.DongjakBatteryBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DongjakBatteryBinService {

    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;
    private final RestClient restClient;
    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public DongjakBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.restClient = RestClient.create();
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }
    public List<ecoLocation> getBatteryBins(){
        String url =
                "https://api.odcloud.kr/api/15038384/v1/"
                        + "uddi:4979ec89-d261-4491-80d0-919d0d8e2d39"
                        + "?page=1&perPage=1000";

        DongjakBatteryBinResponse response =
                restClient
                        .get()
                        .uri(url)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(DongjakBatteryBinResponse.class);

        if (response == null || response.getData() == null) {
            return List.of();
        }

        List<ecoLocation> locations =
                response.getData()
                        .stream()
                        .map(this::convertToEcoLocation)
                        .toList();

        for (ecoLocation location : locations) {

            // 좌표 변환 실패한 데이터는 저장하지 않음
            if (
                    location.getLatitude() == null ||
                            location.getLongitude() == null
            ) {
                continue;
            }

            // 이미 저장된 데이터인지 확인
            int count =
                    ecoLocationMapper
                            .countByRoadAddressAndLocationType(
                                    location.getRoadAddress(),
                                    location.getLocationType()
                            );

            // 중복이 아니면 저장
            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        }

        return locations;
    }
    private ecoLocation convertToEcoLocation(
            DongjakBatteryBinResponse.DataItem dataItem
    ){
        ecoLocation location = new ecoLocation();
        //장소명
        String locationName = dataItem.getBuildingName();
        if (locationName == null || locationName.isBlank()){
            locationName = dataItem.getCategory();
        }
        location.setLocationName(locationName);

        // 기존 폐건전지,폐형광등 데이터와 같은 타입으로 저장
        location.setLocationType("폐건전지·폐형광등 수거함");

        location.setRoadAddress(dataItem.getRoadAddress());
        location.setJibunAddress(dataItem.getJibunAddress());

        // 좌표 검색에 사용할 주소
        String searchAddress = dataItem.getRoadAddress();

        // 도로명주소가 없으면 지번주소 사용
        if (searchAddress == null || searchAddress.isBlank()){
            searchAddress = dataItem.getJibunAddress();
        }

        // 주소가 없으면 좌표 변환 없이 변환
        if (searchAddress == null || searchAddress.isBlank()) {
            return location;
        }

        //카카오 주소 검색
        KakaoAddressResponse kakaoResponse =
                kakaoAddressService.searchAddress(searchAddress);
        //검색 결과 있으면 위도/경도 저장
        if (kakaoResponse != null &&
        kakaoResponse.getDocuments() != null &&
        !kakaoResponse.getDocuments().isEmpty()
        ){
            KakaoAddressResponse.Document document =
                    kakaoResponse.getDocuments().get(0);
            location.setLatitude(
                    new BigDecimal(document.getY())
            );
            location.setLongitude(
                    new BigDecimal(document.getX())
            );
        }
        return location;
    }
}
