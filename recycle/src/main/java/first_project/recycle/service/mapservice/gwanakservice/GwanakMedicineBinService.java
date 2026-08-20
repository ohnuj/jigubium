package first_project.recycle.service.mapservice.gwanakservice;

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
public class GwanakMedicineBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public GwanakMedicineBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    public List<ecoLocation> importMedicineBins() {

        List<ecoLocation> locations = new ArrayList<>();

        try {

            // resources/data 폴더의 관악구 폐의약품 CSV 파일
            ClassPathResource resource =
                    new ClassPathResource(
                            "data/gwanak-medicine-bin.csv"
                    );

            // CSV 파일 CP949 인코딩으로 읽기
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    resource.getInputStream(),
                                    Charset.forName("CP949")
                            )
                    );

            // Commons CSV로 CSV 데이터 읽기
            Iterable<CSVRecord> records =
                    CSVFormat.DEFAULT
                            .builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .setIgnoreSurroundingSpaces(true)
                            .build()
                            .parse(reader);

            for (CSVRecord record : records) {

                // CSV 컬럼 값 가져오기
                String locationName =
                        record.get("설치장소명").trim();

                String roadAddress =
                        record.get("소재지도로명주소").trim();

                // 주소가 없으면 좌표 변환도 불가능하므로 건너뛰기
                if (roadAddress.isEmpty()) {
                    continue;
                }

                ecoLocation location = new ecoLocation();

                // 폐의약품 수거함 기본 정보
                location.setLocationName(locationName);
                location.setLocationType(
                        "폐의약품 수거함"
                );

                location.setRoadAddress(
                        roadAddress
                );

                // 카카오 주소검색 API로 위도/경도 및 지번주소 검색
                KakaoAddressResponse kakaoResponse =
                        kakaoAddressService
                                .searchAddress(roadAddress);

                if (
                        kakaoResponse != null &&
                                kakaoResponse.getDocuments() != null &&
                                !kakaoResponse.getDocuments().isEmpty()
                ) {

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
                }

                /*
                 * DB의 latitude / longitude가 NOT NULL이므로
                 * 좌표를 찾지 못한 데이터는 저장하지 않음
                 */
                if (
                        location.getLatitude() == null ||
                                location.getLongitude() == null
                ) {
                    System.out.println(
                            "좌표 검색 실패: "
                                    + roadAddress
                    );

                    continue;
                }

                locations.add(location);

                // 같은 도로명주소 + 같은 수거함 종류 중복 확인
                int count =
                        ecoLocationMapper
                                .countByRoadAddressAndLocationType(
                                        location.getRoadAddress(),
                                        location.getLocationType()
                                );

                // DB에 없는 경우에만 저장
                if (count == 0) {

                    ecoLocationMapper
                            .insertEcoLocation(location);
                }
            }

            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        System.out.println(
                "관악구 폐의약품 수거함 변환 개수: "
                        + locations.size()
        );

        return locations;
    }
}