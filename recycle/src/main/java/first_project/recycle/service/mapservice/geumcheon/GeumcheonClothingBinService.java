package first_project.recycle.service.mapservice.geumcheon;
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
public class GeumcheonClothingBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public GeumcheonClothingBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 금천구 의류수거함 CSV 호출 및 DB 저장
    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/geumcheon-clothing-bin.csv"
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
                 * data[1] = 행정동
                 * data[2] = 도로명주소
                 * data[3] = 위도
                 * data[4] = 경도
                 */
                String[] data =
                        line.split(",", -1);

                if (data.length < 5) {
                    continue;
                }


                String adminDong =
                        data[1].trim();

                String roadAddress =
                        data[2].trim();

                String latitudeValue =
                        data[3].trim();

                String longitudeValue =
                        data[4].trim();


                if (roadAddress.isBlank()) {
                    continue;
                }


                BigDecimal latitude;
                BigDecimal longitude;


                /*
                 * CSV에 좌표가 있는 경우
                 * 원본 좌표 그대로 사용
                 */
                if (
                        !latitudeValue.isBlank() &&
                                !longitudeValue.isBlank()
                ) {

                    latitude =
                            new BigDecimal(latitudeValue);

                    longitude =
                            new BigDecimal(longitudeValue);

                } else {

                    /*
                     * 좌표가 없는 데이터만
                     * Kakao 주소검색 API로 좌표 보완
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
                                "금천구 의류수거함 좌표 변환 실패: "
                                        + roadAddress
                        );

                        continue;
                    }


                    KakaoAddressResponse.Document document =
                            response
                                    .getDocuments()
                                    .get(0);


                    // Kakao API: y = 위도, x = 경도
                    latitude =
                            new BigDecimal(
                                    document.getY()
                            );

                    longitude =
                            new BigDecimal(
                                    document.getX()
                            );
                }


                EcoLocation location =
                        new EcoLocation();


                // 의류수거함 명칭 / 유형 통일
                location.setLocationName(
                        "의류수거함"
                );

                location.setLocationType(
                        "의류수거함"
                );


                location.setAdminDong(
                        adminDong
                );


                location.setRoadAddress(
                        roadAddress
                );


                // 별도 지번주소 없음
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
