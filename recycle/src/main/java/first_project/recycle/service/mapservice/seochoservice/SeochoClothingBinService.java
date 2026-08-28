package first_project.recycle.service.mapservice.seochoservice;
import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.seocho.SeochoClothingBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeochoClothingBinService {

    private final EcoLocationMapper ecoLocationMapper;
    private final RestClient restClient;
    private final String serviceKey;

    // 서초구 의류수거함 API
    private static final String API_URL =
            "https://api.odcloud.kr/api/15157877/v1/" +
                    "uddi:d1120593-6b1e-4b85-8682-96d467835024" +
                    "?page=1&perPage=1000";


    public SeochoClothingBinService(
            EcoLocationMapper ecoLocationMapper,
            @Value("${PUBLIC_DATA_SERVICE_KEY}") String serviceKey
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
        this.serviceKey = serviceKey;
        this.restClient = RestClient.create();
    }


    // 서초구 의류수거함 API 호출 및 DB 저장
    public List<EcoLocation> getClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();


        // 공공데이터 API 호출
        SeochoClothingBinResponse response =
                restClient.get()
                        .uri(API_URL)
                        .header(
                                "Authorization",
                                "Infuser " + serviceKey
                        )
                        .retrieve()
                        .body(SeochoClothingBinResponse.class);


        // 응답 데이터가 없으면 빈 리스트 반환
        if (
                response == null ||
                        response.getData() == null
        ) {
            return savedList;
        }


        // API 데이터 한 건씩 DB 저장
        for (
                SeochoClothingBinResponse.Item item
                : response.getData()
        ) {

            /*
             * 좌표가 없으면 지도에 표시할 수 없으므로 제외
             */
            if (
                    item.getLatitude() == null ||
                            item.getLatitude().isBlank() ||
                            item.getLongitude() == null ||
                            item.getLongitude().isBlank()
            ) {
                continue;
            }


            EcoLocation location =
                    new EcoLocation();


            /*
             * 설치장소명이 있으면 실제 장소명 사용
             * 없으면 기본 명칭 사용
             */
            if (
                    item.getLocationName() == null ||
                            item.getLocationName().isBlank()
            ) {

                location.setLocationName(
                        "의류수거함"
                );

            } else {

                location.setLocationName(
                        item.getLocationName()
                );
            }


            // 장소 유형은 프로젝트 기준으로 통일
            location.setLocationType(
                    "의류수거함"
            );


            // 행정동
            location.setAdminDong(
                    item.getAdminDong()
            );


            // 도로명주소
            if (
                    item.getRoadAddress() == null ||
                            item.getRoadAddress().isBlank()
            ) {

                location.setRoadAddress(null);

            } else {

                location.setRoadAddress(
                        item.getRoadAddress()
                );
            }


            // 지번주소
            if (
                    item.getJibunAddress() == null ||
                            item.getJibunAddress().isBlank()
            ) {

                location.setJibunAddress(null);

            } else {

                location.setJibunAddress(
                        item.getJibunAddress()
                );
            }


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