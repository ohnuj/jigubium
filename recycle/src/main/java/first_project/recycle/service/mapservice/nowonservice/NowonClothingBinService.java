package first_project.recycle.service.mapservice.nowonservice;
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
public class NowonClothingBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public NowonClothingBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 노원구 의류수거함 CSV 호출 및 DB 저장
    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/nowon-clothing-bin.csv"
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
                 * data[2] = 지번주소
                 */
                String[] data =
                        line.split(",", -1);

                if (data.length < 3) {
                    continue;
                }

                String adminDong =
                        data[1].trim();

                String jibunAddress =
                        data[2].trim();


                // 주소가 없으면 좌표 변환 불가능
                if (jibunAddress.isBlank()) {
                    continue;
                }


                // 지번주소 → 위도 / 경도 변환
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                jibunAddress
                        );


                // 카카오 주소검색 실패 시 해당 데이터 제외
                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "노원구 의류수거함 좌표 변환 실패: "
                                    + jibunAddress
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


                // 의류수거함 명칭 및 유형 통일
                location.setLocationName(
                        "의류수거함"
                );

                location.setLocationType(
                        "의류수거함"
                );


                // 행정동
                location.setAdminDong(
                        adminDong
                );


                /*
                 * 이번 데이터는 도로명주소가 아니라
                 * 지번주소 데이터이므로 roadAddress는 null
                 */
                location.setRoadAddress(
                        null
                );

                location.setJibunAddress(
                        jibunAddress
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
