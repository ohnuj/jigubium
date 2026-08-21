package first_project.recycle.service.mapservice.seodaemunservice;

import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationdto.seodaemun.SeodaemunClothingBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
//서대문구 의류수거함 API
//↓
//DataItem
//↓
//ecoLocation 변환
//↓
//위도/경도 확인
//↓
//도로명주소 + 장소타입 중복 확인
//↓
//DB 저장
@Service
public class SeodaemunClothingBinService {

    // 공공데이터 API 인증키
    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;

    // 외부 API 호출용
    private final RestClient restClient = RestClient.create();

    // DB 저장용 Mapper
    private final EcoLocationMapper ecoLocationMapper;

    // 생성자 주입
    public SeodaemunClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }

    public List<ecoLocation> getClothingBins() {

        SeodaemunClothingBinResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.odcloud.kr")
                        .path("/api/15068863/v1/uddi:73e87352-5656-4a86-81d6-8ff8176cf7f0")
                        .queryParam("page", 1)
                        .queryParam("perPage", 1000)
                        .build()
                )
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(SeodaemunClothingBinResponse.class);

        if (response == null || response.getData() == null) {
            return List.of();
        }

        List<ecoLocation> locations = response
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

    // 서대문구 의류수거함 데이터를 ecoLocation으로 변환
    private ecoLocation convertToEcoLocation(
            SeodaemunClothingBinResponse.DataItem dataItem
    ) {

        ecoLocation location = new ecoLocation();

        location.setLocationName("의류수거함");
        location.setLocationType("의류수거함");

        location.setAdminDong(dataItem.getAdminDong());
        location.setRoadAddress(dataItem.getRoadAddress());

        // 위도
        if (
                dataItem.getLatitude() != null &&
                        !dataItem.getLatitude().isBlank()
        ) {
            location.setLatitude(
                    new BigDecimal(dataItem.getLatitude())
            );
        }

        // 경도
        if (
                dataItem.getLongitude() != null &&
                        !dataItem.getLongitude().isBlank()
        ) {
            location.setLongitude(
                    new BigDecimal(dataItem.getLongitude())
            );
        }

        return location;
    }
}
