package first_project.recycle.service.mapservice.dongjakservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.dongjak.DongjakClothingBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;


/**
 * api에서 위도 경도를 제공해줘서 kakaoAddressService 이용 안함
 */
@Service
public class DongjakClothingBinService {

    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    // api에 http 요청 보내는 객체
    private final RestClient restClient;
    // 데이터를 저장하거나 중복 여부확인할 때 사용할 객체
    private final EcoLocationMapper ecoLocationMapper;

    public DongjakClothingBinService(
            EcoLocationMapper ecoLocationMapper) {
        this.restClient = RestClient.create();
        this.ecoLocationMapper = ecoLocationMapper;
    }

    // 데이터를 DB에 저장하는 메서드
    public List<EcoLocation> getClothingBins() {
        String url = "https://api.odcloud.kr/api/15068021/v1/"  // swagger
                + "uddi:cd670738-3614-4e80-a341-f495bb1f91e0"   // UUID
                + "?page=1&perPage=1000";   // 기본으로 넣어줘야 하는 파라미터
        DongjakClothingBinResponse response = restClient
                .get()
                .uri(url)
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(DongjakClothingBinResponse.class); //api가 보내준 JSON응답을 Response 객체로 자동 변환
        // api 응답이 없을 시 빈 목록을 반환 nullpointerException 방지 위한 장치
        if (response == null || response.getData() == null) {
            return List.of();
        }

        // 공공데이터의 DataItem 목록을 ecoLocation 목록으로 변환
        List<EcoLocation> locations =
                response.getData()
                        .stream()
                        .map(this::convertToEcoLocation)
                        .toList();

        // 변환 완료된 데이터를 하나씩 리스트에서 끄집어 내 정상적일 경우 DB에 저장
        for (EcoLocation location : locations) {
            if (location.getRoadAddress() == null ||
                    location.getRoadAddress().isBlank() ||
                    location.getLatitude() == null ||
                    location.getLongitude() == null) {
                continue;
            }
            // 중복 체크할 것
            int count =
                    ecoLocationMapper
                            .countByRoadAddressAndLocationType(location.getRoadAddress(),
                                    location.getLocationType());
            // 중복되지 않은 데이터만 DB에 저장
            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        }

        return locations;


    }
    private EcoLocation convertToEcoLocation(
            DongjakClothingBinResponse.DataItem dataItem
    ){
        EcoLocation location = new EcoLocation();
        // 화면에 표시할 장소명과 장소 타입은 "의류수거함"으로 통일
        location.setLocationName("의류수거함");
        location.setLocationType("의류수거함");
        // api의 행정동 값을 저장
        location.setAdminDong(dataItem.getAdminDong());

        // 해당 api는 하나의 주소만 제공하므로 도로명 주소만 채우고 지번 주소는 넣지 않는다
        location.setRoadAddress(dataItem.getAddress());
        //위도 경도 문자열 BigDecimal로 변환
        location.setLatitude(parseCoordinate(dataItem.getLatitude()));
        location.setLongitude(parseCoordinate(dataItem.getLongitude()));
        return location;
    }

    // 위도 경도를 bigdecimal로 변환해주는 메서드
    private BigDecimal parseCoordinate(String coordinate) {
        //좌표가 null일 시 그냥 null로 변환
        if (coordinate == null || coordinate.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(coordinate.trim());
        }catch (NumberFormatException e){
            //숫자가 아닌 값이 들어온다면 null로 변환시킨다
            return null;
        }
    }
}
