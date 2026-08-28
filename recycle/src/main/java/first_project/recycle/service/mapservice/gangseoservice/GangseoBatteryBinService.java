package first_project.recycle.service.mapservice.gangseoservice;
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
public class GangseoBatteryBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;


    public GangseoBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 강서구 폐건전지·폐형광등 CSV 호출 및 DB 저장
    public List<EcoLocation> importBatteryBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/gangseo-battery-bin.csv"
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

            // CSV 헤더 건너뛰기
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

                String address =
                        data[2].trim();

                String detail =
                        data[3].trim();


                /*
                 * 원본 데이터 중 7건은
                 * 주소 없이 장소명만 제공됨
                 *
                 * 임의로 주소를 추측해서 저장하지 않고
                 * 콘솔에 출력한 뒤 제외
                 */
                if (address.isBlank()) {

                    System.out.println(
                            "강서구 폐건전지 주소 없음: "
                                    + adminDong
                                    + " / "
                                    + detail
                    );

                    continue;
                }


                /*
                 * 주소 → 위도 / 경도 변환
                 */
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                address
                        );


                /*
                 * 카카오 주소검색 결과가 없으면
                 * DB에 저장하지 않고 실패 주소 출력
                 */
                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "강서구 폐건전지 좌표 변환 실패: "
                                    + address
                    );

                    continue;
                }


                KakaoAddressResponse.Document document =
                        response
                                .getDocuments()
                                .get(0);


                /*
                 * Kakao 주소검색 API
                 *
                 * y = 위도
                 * x = 경도
                 */
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
                 * 상세위치가 있으면 장소명으로 사용
                 *
                 * 예:
                 * 염창동주민센터
                 * 대림아파트 뒤편 벽면
                 * 강서구청
                 */
                if (detail.isBlank()) {

                    location.setLocationName(
                            "폐건전지·폐형광등 수거함"
                    );

                } else {

                    location.setLocationName(
                            detail
                    );
                }


                // 프로젝트 장소 유형 통일
                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );


                // 행정동 저장
                location.setAdminDong(
                        adminDong
                );


                /*
                 * 주소 종류 구분
                 *
                 * 대부분 도로명주소지만
                 * "서울특별시 강서구 화곡동 1008-32"
                 * 같은 지번주소도 존재함
                 */
                if (
                        address.matches(
                                ".*[가-힣]+동\\s+\\d+(?:-\\d+)?$"
                        )
                ) {

                    location.setRoadAddress(
                            null
                    );

                    location.setJibunAddress(
                            address
                    );

                } else {

                    location.setRoadAddress(
                            address
                    );

                    location.setJibunAddress(
                            null
                    );
                }


                // 변환된 좌표 저장
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
