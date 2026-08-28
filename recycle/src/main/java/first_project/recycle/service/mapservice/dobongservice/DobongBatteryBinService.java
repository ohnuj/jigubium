package first_project.recycle.service.mapservice.dobongservice;
import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.dobong.DobongBatteryBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DobongBatteryBinService {

    private final EcoLocationMapper ecoLocationMapper;
    private final RestClient restClient;
    private final String serviceKey;

    // 도봉구 폐건전지·폐형광등 수거함 API
    private static final String API_URL =
            "https://api.odcloud.kr/api/15038207/v1/" +
                    "uddi:6b4dca84-a365-4b89-a269-90ee8e70d716" +
                    "?page=1&perPage=1000";


    public DobongBatteryBinService(
            EcoLocationMapper ecoLocationMapper,
            @Value("${PUBLIC_DATA_SERVICE_KEY}") String serviceKey
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
        this.serviceKey = serviceKey;
        this.restClient = RestClient.create();
    }


    // 도봉구 폐건전지·폐형광등 API 호출 및 DB 저장
    public List<EcoLocation> getBatteryBins() {

        List<EcoLocation> savedList = new ArrayList<>();


        // 공공데이터 API 호출
        DobongBatteryBinResponse response =
                restClient.get()
                        .uri(API_URL)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(DobongBatteryBinResponse.class);


        // 응답 데이터가 없으면 빈 리스트 반환
        if (
                response == null ||
                        response.getData() == null
        ) {
            return savedList;
        }


        // API 데이터 한 건씩 DB 저장
        for (
                DobongBatteryBinResponse.Item item
                : response.getData()
        ) {

            /*
             * 좌표가 없는 데이터는
             * 지도에 표시할 수 없으므로 제외
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


            /*
             * 장소명이 있으면 실제 장소명 사용
             * 없으면 기본 수거함 명칭 사용
             */
            if (
                    item.getLocationName() == null ||
                            item.getLocationName().isBlank()
            ) {
                location.setLocationName(
                        "폐건전지·폐형광등 수거함"
                );
            } else {
                location.setLocationName(
                        item.getLocationName()
                );
            }


            // 프로젝트에서 사용하는 장소 유형으로 통일
            location.setLocationType(
                    "폐건전지·폐형광등 수거함"
            );


            // 행정동
            location.setAdminDong(
                    item.getAdminDong()
            );


            // API에서 제공하는 주소
            location.setRoadAddress(
                    item.getAddress()
            );

            location.setJibunAddress(
                    null
            );


            // API에서 제공하는 좌표 그대로 저장
            location.setLatitude(
                    new BigDecimal(
                            item.getLatitude()
                    )
            );

            location.setLongitude(
                    new BigDecimal(
                            item.getLongitude()
                    )
            );


            // DB 저장
            ecoLocationMapper.insertEcoLocation(
                    location
            );

            savedList.add(
                    location
            );
        }


        return savedList;
    }
}
