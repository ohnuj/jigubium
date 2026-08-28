package first_project.recycle.service.mapservice.gangdongservice;
import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GangdongMedicineBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public GangdongMedicineBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 강동구 폐의약품 수거함 CSV 호출 및 DB 저장
    public List<EcoLocation> importMedicineBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/gangdong-medicine-bin.csv"
                );


        try (
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            // 헤더 건너뛰기
            br.readLine();

            String line;

            while ((line = br.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }


                /*
                 * CSV 구조
                 *
                 * data[0] = 연번
                 * data[1] = 설치장소명
                 * data[2] = 도로명주소
                 * data[3] = 상세위치
                 */
                String[] data =
                        line.split(",", -1);

                if (data.length < 4) {
                    continue;
                }


                String locationName =
                        data[1].trim();

                String roadAddress =
                        data[2].trim();


                // 주소가 없으면 좌표 변환 불가
                if (roadAddress.isBlank()) {
                    continue;
                }


                /*
                 * Kakao 주소 검색으로
                 * 도로명주소 → 위도/경도 변환
                 */
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                roadAddress
                        );


                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "강동구 폐의약품 수거함 좌표 변환 실패: "
                                    + roadAddress
                    );

                    continue;
                }


                KakaoAddressResponse.Document document =
                        response
                                .getDocuments()
                                .get(0);


                // Kakao API: y = 위도, x = 경도
                BigDecimal latitude =
                        new BigDecimal(
                                document.getY()
                        );

                BigDecimal longitude =
                        new BigDecimal(
                                document.getX()
                        );


                EcoLocation location =
                        new EcoLocation();


                /*
                 * 실제 설치장소명 사용
                 *
                 * 예:
                 * 강동구보건소 3층 보건의료과
                 * 천호3동 주민센터
                 * 중앙보훈병원(...)
                 */
                if (locationName.isBlank()) {

                    location.setLocationName(
                            "폐의약품 수거함"
                    );

                } else {

                    location.setLocationName(
                            locationName
                    );
                }


                // 지도 필터용 유형 통일
                location.setLocationType(
                        "폐의약품 수거함"
                );


                // 원본 데이터에 행정동 컬럼 없음
                location.setAdminDong(
                        null
                );


                location.setRoadAddress(
                        roadAddress
                );


                // 지번주소는 제공되지 않음
                location.setJibunAddress(
                        null
                );


                location.setLatitude(
                        latitude
                );

                location.setLongitude(
                        longitude
                );


                // DB 저장
                ecoLocationMapper.insertEcoLocation(
                        location
                );


                savedList.add(
                        location
                );
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return savedList;
    }
}
