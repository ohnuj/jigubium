package first_project.recycle.service.mapservice.eunpyeongservice;
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
public class EunpyeongClothingBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public EunpyeongClothingBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 은평구 의류수거함 CSV 호출 및 DB 저장
    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/eunpyeong-clothing-bin.csv"
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
                 * data[2] = 주소
                 * data[3] = 상세위치
                 */
                String[] data =
                        line.split(",", -1);

                if (data.length < 4) {
                    continue;
                }

                String adminDong =
                        data[1].trim();

                String roadAddress =
                        data[2].trim();


                // 주소가 없으면 좌표 변환 불가능
                if (roadAddress.isBlank()) {
                    continue;
                }


                // 주소 → 위도 / 경도 변환
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                roadAddress
                        );


                // 카카오 주소검색 실패 시 해당 데이터 제외
                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "은평구 의류수거함 좌표 변환 실패: "
                                    + roadAddress
                    );

                    continue;
                }


                KakaoAddressResponse.Document document =
                        response
                                .getDocuments()
                                .get(0);


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


                // 의류수거함 명칭 / 유형 통일
                location.setLocationName(
                        "의류수거함"
                );

                location.setLocationType(
                        "의류수거함"
                );


                // 행정동 저장
                location.setAdminDong(
                        adminDong
                );


                // 정리한 주소 저장
                location.setRoadAddress(
                        roadAddress
                );

                // 별도 지번주소 없음
                location.setJibunAddress(
                        null
                );


                // 카카오 API 좌표 저장
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
