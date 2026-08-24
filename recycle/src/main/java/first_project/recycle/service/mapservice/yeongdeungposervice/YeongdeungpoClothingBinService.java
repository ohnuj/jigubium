package first_project.recycle.service.mapservice.yeongdeungposervice;

import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 영등포구는 오픈 API 허가를 받지 못해
 * CP949로 인코딩된 CSV 파일을 사용한다.
 */
@Service
public class YeongdeungpoClothingBinService {

    // 좌표가 없는 CSV 데이터를 주소로 검색해 위도와 경도를 가져오는 서비스
    private final KakaoAddressService kakaoAddressService;

    // 데이터 저장 및 중복 확인에 사용하는 Mapper
    private final EcoLocationMapper ecoLocationMapper;

    // 생성자 주입
    public YeongdeungpoClothingBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 영등포구 의류수거함 CSV를 읽어 DB에 저장하는 메서드
     *
     * 처리 순서
     * 1. resources/data 폴더의 CSV 파일 불러오기
     * 2. CP949 인코딩으로 파일 읽기
     * 3. CSV의 각 행을 ecoLocation으로 변환
     * 4. CSV에 좌표가 있으면 해당 좌표 사용
     * 5. 좌표가 없으면 카카오 주소 검색으로 좌표 변환
     * 6. 주소와 좌표가 정상인 데이터만 선별
     * 7. 기존 데이터와 중복되는지 확인
     * 8. 중복되지 않은 데이터만 DB에 저장
     */
    public List<ecoLocation> importClothingBins() {

        List<ecoLocation> locations = new ArrayList<>();

        /*
         * try-with-resources
         *
         * 괄호 안에서 BufferedReader를 생성하면
         * CSV 처리가 끝난 후 자동으로 닫힌다.
         */
        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new ClassPathResource(
                                                "data/yeongdeungpo-clothing-bin.csv"
                                        ).getInputStream(),
                                        Charset.forName("CP949")
                                )
                        )
        ) {
            // 첫 번째 줄은 컬럼명이므로 건너뛴다.
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                /*
                 * -1을 사용하면 빈 위도·경도 컬럼도
                 * 배열에서 사라지지 않고 유지된다.
                 */
                String[] data = line.split(",", -1);

                // 영등포구 CSV는 6개 컬럼으로 구성됨
                if (data.length < 6) {
                    continue;
                }

                String adminDong = data[0].trim();
                String roadAddress = data[1].trim();
                String jibunAddress = data[2].trim();
                String latitudeValue = data[3].trim();
                String longitudeValue = data[4].trim();

                /*
                 * 도로명주소가 없으면 지번주소를 대표 주소로 사용한다.
                 * 두 주소가 모두 없으면 해당 행을 건너뛴다.
                 */
                String mainAddress = roadAddress;

                if (mainAddress.isEmpty()) {
                    mainAddress = jibunAddress;
                }

                if (mainAddress.isEmpty()) {
                    continue;
                }

                ecoLocation location = new ecoLocation();

                location.setLocationName("의류수거함");
                location.setLocationType("의류수거함");

                location.setAdminDong(
                        adminDong.isEmpty() ? null : adminDong
                );

                location.setRoadAddress(mainAddress);

                location.setJibunAddress(
                        jibunAddress.isEmpty() ? null : jibunAddress
                );

                /*
                 * CSV에 있는 위도와 경도를 BigDecimal로 변환한다.
                 * 빈 값이나 잘못된 값은 null로 변환된다.
                 */
                location.setLatitude(
                        parseCoordinate(latitudeValue)
                );

                location.setLongitude(
                        parseCoordinate(longitudeValue)
                );

                /*
                 * CSV에 위도 또는 경도가 없다면
                 * 카카오 주소 검색으로 좌표를 보완한다.
                 */
                if (
                        location.getLatitude() == null ||
                                location.getLongitude() == null
                ) {
                    setCoordinatesFromAddress(
                            location,
                            roadAddress,
                            jibunAddress
                    );
                }

                /*
                 * 카카오 주소 검색 후에도 좌표가 없으면
                 * DB에 저장하지 않는다.
                 */
                if (
                        location.getLatitude() == null ||
                                location.getLongitude() == null
                ) {
                    continue;
                }

                locations.add(location);

                // 동일한 주소와 장소 유형이 이미 저장돼 있는지 확인
                int count =
                        ecoLocationMapper
                                .countByRoadAddressAndLocationType(
                                        location.getRoadAddress(),
                                        location.getLocationType()
                                );

                // 중복되지 않은 데이터만 DB에 저장
                if (count == 0) {
                    ecoLocationMapper.insertEcoLocation(location);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return locations;
    }

    /**
     * CSV에 좌표가 없을 경우
     * 도로명주소 또는 지번주소를 사용해 좌표를 찾는다.
     */
    private void setCoordinatesFromAddress(
            ecoLocation location,
            String roadAddress,
            String jibunAddress
    ) {
        // 도로명주소로 먼저 검색
        KakaoAddressResponse kakaoResponse =
                searchAddress(roadAddress);

        // 도로명주소 검색에 실패하면 지번주소로 다시 검색
        if (!hasSearchResult(kakaoResponse)) {
            kakaoResponse = searchAddress(jibunAddress);
        }

        // 두 주소 모두 검색에 실패하면 좌표를 설정하지 않음
        if (!hasSearchResult(kakaoResponse)) {
            return;
        }

        KakaoAddressResponse.Document document =
                kakaoResponse.getDocuments().get(0);

        // 카카오 API의 Y는 위도, X는 경도
        location.setLatitude(
                parseCoordinate(document.getY())
        );

        location.setLongitude(
                parseCoordinate(document.getX())
        );
    }

    /**
     * 주소가 있을 때만 카카오 주소 검색을 실행한다.
     */
    private KakaoAddressResponse searchAddress(String address) {

        if (address == null || address.isBlank()) {
            return null;
        }

        return kakaoAddressService.searchAddress(address);
    }

    /**
     * 카카오 주소 검색 결과가 존재하는지 확인한다.
     */
    private boolean hasSearchResult(
            KakaoAddressResponse kakaoResponse
    ) {
        return kakaoResponse != null &&
                kakaoResponse.getDocuments() != null &&
                !kakaoResponse.getDocuments().isEmpty();
    }

    /**
     * 문자열 좌표를 BigDecimal로 안전하게 변환한다.
     */
    private BigDecimal parseCoordinate(String coordinate) {

        if (coordinate == null || coordinate.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(coordinate.trim());

        } catch (NumberFormatException e) {
            return null;
        }
    }
}