package first_project.recycle.service.mapservice.gangbukservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.gangbuk.GangbukClothingBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GangbukClothingBinService {

    private final EcoLocationMapper ecoLocationMapper;
    private final RestClient restClient;
    private final String serviceKey;

    // 강북구 의류수거함 API 주소
    private static final String API_URL =
            "https://api.odcloud.kr/api/15138051/v1/" +
                    "uddi:1dfe0f6e-5e00-4f17-83d2-aad6a545f5e7" +
                    "?page=1&perPage=1000";


    public GangbukClothingBinService(
            EcoLocationMapper ecoLocationMapper,
            @Value("${PUBLIC_DATA_SERVICE_KEY}") String serviceKey
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
        this.serviceKey = serviceKey;
        this.restClient = RestClient.create();
    }


    // 강북구 의류수거함 API 호출 및 DB 저장
    public List<EcoLocation> getClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();


        // 공공데이터 API 호출
        GangbukClothingBinResponse response =
                restClient.get()
                        .uri(API_URL)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(GangbukClothingBinResponse.class);


        // API 응답이 없으면 빈 목록 반환
        if (
                response == null ||
                        response.getData() == null
        ) {
            return savedList;
        }


        // API 데이터 한 건씩 DB 저장
        for (
                GangbukClothingBinResponse.Item item
                : response.getData()
        ) {

            /*
             * 위도 / 경도가 없으면
             * 지도에 표시할 수 없으므로 저장하지 않음
             */
            if (
                    item.getLatitude() == null ||
                            item.getLatitude().isBlank() ||
                            item.getLongitude() == null ||
                            item.getLongitude().isBlank()
            ) {
                continue;
            }


            EcoLocation location = new EcoLocation();

            // 의류수거함은 장소명 / 장소유형 통일
            location.setLocationName("의류수거함");
            location.setLocationType("의류수거함");

            // API에서 제공되는 행정동
            location.setAdminDong(item.getAdminDong());

            // API에서 제공되는 도로명주소
            location.setRoadAddress(item.getRoadAddress());

            // 별도 지번주소는 제공되지 않음
            location.setJibunAddress(null);

            // API 좌표를 그대로 사용
            location.setLatitude(
                    new BigDecimal(item.getLatitude())
            );

            location.setLongitude(
                    new BigDecimal(item.getLongitude())
            );


            // eco_location 테이블에 저장
            ecoLocationMapper.insertEcoLocation(location);

            savedList.add(location);
        }


        return savedList;
    }
}