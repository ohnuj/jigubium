package first_project.recycle.service.mapservice.wasteelectronicsservice;

import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
public class WasteElectronicsService {

    private final EcoLocationMapper ecoLocationMapper;
    private final KakaoAddressService kakaoAddressService;

    public WasteElectronicsService(
            EcoLocationMapper ecoLocationMapper,
            KakaoAddressService kakaoAddressService
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
        this.kakaoAddressService = kakaoAddressService;
    }


    // 폐가전 CSV를 읽어서 서울 데이터만 DB에 저장
    public List<ecoLocation> importWasteElectronics() {

        List<ecoLocation> locations = new ArrayList<>();

        try {

            // resources/data의 CSV 파일 가져오기
            ClassPathResource resource =
                    new ClassPathResource(
                            "data/waste-electronics.csv"
                    );

            /*
             * 원본 CSV가 EUC-KR 계열이므로
             * CP949로 읽음
             */
            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            resource.getInputStream(),
                                            Charset.forName("CP949")
                                    )
                            )
            ) {

                // Commons CSV로 파일 읽기
                Iterable<CSVRecord> records =
                        CSVFormat.DEFAULT
                                .builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreSurroundingSpaces(true)
                                .build()
                                .parse(reader);


                for (CSVRecord record : records) {

                    /*
                     * CSV 컬럼
                     *
                     * 0 = 순번
                     * 1 = 상호명
                     * 2 = 수거종류
                     * 3 = 수거방법
                     * 4 = 수거장소(주소)
                     * 5 = 장소구분
                     * 6 = 수거비용
                     */

                    String locationName =
                            record.get(1).trim();

                    String collectionType =
                            record.get(2).trim();

                    // 폐휴대폰 수거처는 제외
                    if (collectionType.contains("폐휴대폰")) {
                        continue;
                    }

                    String roadAddress =
                            record.get(4).trim();


                    // 주소가 없으면 제외
                    if (roadAddress.isEmpty()) {
                        continue;
                    }


                    /*
                     * 서울시 데이터만 사용
                     *
                     * "서울 노원구 ..."
                     * "서울특별시 관악구 ..."
                     *
                     * 둘 다 startsWith("서울")로 걸러짐
                     */
                    if (!roadAddress.startsWith("서울")) {
                        continue;
                    }


                    /*
                     * 이미 DB에 존재하는 주소인지 먼저 확인
                     *
                     * 이렇게 하면 서버를 재실행하거나
                     * import API를 다시 호출했을 때
                     * 카카오 API를 또 호출하지 않아도 됨
                     */
                    int count =
                            ecoLocationMapper
                                    .countByRoadAddressAndLocationType(
                                            roadAddress,
                                            "폐가전 수거함"
                                    );

                    if (count > 0) {
                        continue;
                    }


                    ecoLocation location =
                            new ecoLocation();

                    // 상호명
                    location.setLocationName(
                            locationName
                    );

                    /*
                     * 폐휴대폰, 중소폐가전 등을
                     * 지도에서는 모두 폐가전 수거함으로 통일
                     */
                    location.setLocationType(
                            "폐가전 수거함"
                    );

                    location.setRoadAddress(
                            roadAddress
                    );


                    /*
                     * CSV에 위도/경도가 없기 때문에
                     * 카카오 주소 검색 API 사용
                     */
                    try {

                        KakaoAddressResponse kakaoResponse =
                                kakaoAddressService
                                        .searchAddress(
                                                roadAddress
                                        );


                        // 주소 검색 실패
                        if (
                                kakaoResponse == null ||
                                        kakaoResponse.getDocuments() == null ||
                                        kakaoResponse.getDocuments().isEmpty()
                        ) {

                            System.out.println(
                                    "폐가전 좌표 검색 실패: "
                                            + roadAddress
                            );

                            continue;
                        }


                        KakaoAddressResponse.Document document =
                                kakaoResponse
                                        .getDocuments()
                                        .get(0);


                        // 위도
                        location.setLatitude(
                                new BigDecimal(
                                        document.getY()
                                )
                        );


                        // 경도
                        location.setLongitude(
                                new BigDecimal(
                                        document.getX()
                                )
                        );


                        // 지번주소
                        if (document.getAddress() != null) {

                            location.setJibunAddress(
                                    document
                                            .getAddress()
                                            .getAddressName()
                            );
                        }


                    } catch (Exception e) {

                        /*
                         * 한 주소의 카카오 검색이 실패하더라도
                         * 전체 CSV 처리가 중단되지 않도록 함
                         */
                        System.out.println(
                                "폐가전 주소 변환 중  오류: "
                                        + roadAddress
                        );

                        continue;
                    }


                    // 좌표가 없으면 DB 저장하지 않음
                    if (
                            location.getLatitude() == null ||
                                    location.getLongitude() == null
                    ) {
                        continue;
                    }


                    // DB 저장
                    ecoLocationMapper
                            .insertEcoLocation(
                                    location
                            );


                    locations.add(
                            location
                    );


                    // 진행 상황 확인용
                    if (locations.size() % 100 == 0) {

                        System.out.println(
                                "서울 폐가전 저장 개수: "
                                        + locations.size()
                        );
                    }
                }
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        System.out.println(
                "서울 폐가전 최종 저장 개수: "
                        + locations.size()
        );


        return locations;
    }
}
