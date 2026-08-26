package first_project.recycle.service.mapservice.maposervice;

import first_project.recycle.domain.EcoLocation;
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

// CSV에 좌표가 없어 카카오 주소 검색을 사용
@Service
public class MapoBatteryBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public MapoBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 마포구 폐건전지 CSV 불러오기
     * 2. CSV 데이터를 ecoLocation으로 변환
     * 3. 도로명주소를 카카오 API로 검색
     * 4. 중복되지 않은 정상 데이터만 DB에 저장
     */
    public List<EcoLocation> importBatteryBins() {

        List<EcoLocation> locations = new ArrayList<>();

        // 1. 정리된 CSV 파일을 CP949로 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/mapo-battery-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 첫 번째 컬럼명 행 건너뛰기
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",", -1);

                // 정리된 CSV는 총 11개 컬럼
                if (data.length < 11) {
                    continue;
                }

                // 2. CSV에서 필요한 값 가져오기
                String adminDong = data[3].trim();
                String roadAddress = data[4].trim();
                String category = data[5].trim();
                String detailName = data[6].trim();

                // 주소가 없으면 좌표 검색이 불가능하므로 제외
                if (roadAddress.isEmpty()) {
                    continue;
                }

                EcoLocation location = new EcoLocation();

                /*
                 * 장소명 우선순위
                 * 1. 세부위치
                 * 2. 도로명주소
                 * 3. 구분
                 * 4. 기본 이름
                 */
                String locationName = detailName;

                if (locationName.isEmpty()) {
                    locationName = roadAddress;
                }

                if (locationName.isEmpty()) {
                    locationName = category;
                }

                if (locationName.isEmpty()) {
                    locationName =
                            "폐건전지·폐형광등 수거함";
                }

                location.setLocationName(locationName);
                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );

                location.setAdminDong(
                        adminDong.isEmpty() ? null : adminDong
                );

                location.setRoadAddress(roadAddress);

                // 3. 도로명주소를 카카오 API로 검색
                KakaoAddressResponse kakaoResponse =
                        kakaoAddressService
                                .searchAddress(roadAddress);

                if (hasSearchResult(kakaoResponse)) {

                    KakaoAddressResponse.Document document =
                            kakaoResponse
                                    .getDocuments()
                                    .get(0);

                    location.setLatitude(
                            parseCoordinate(document.getY())
                    );

                    location.setLongitude(
                            parseCoordinate(document.getX())
                    );
                }

                // 좌표 검색에 실패한 데이터는 저장하지 않음
                if (
                        location.getLatitude() == null ||
                                location.getLongitude() == null
                ) {
                    continue;
                }

                locations.add(location);

                // 4. 같은 주소와 장소 유형이 있는지 확인
                int count =
                        ecoLocationMapper
                                .countByRoadAddressAndLocationType(
                                        location.getRoadAddress(),
                                        location.getLocationType()
                                );

                if (count == 0) {
                    ecoLocationMapper.insertEcoLocation(location);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return locations;
    }

    // 카카오 주소 검색 결과 확인
    private boolean hasSearchResult(
            KakaoAddressResponse kakaoResponse
    ) {
        return kakaoResponse != null &&
                kakaoResponse.getDocuments() != null &&
                !kakaoResponse.getDocuments().isEmpty();
    }

    // 문자열 좌표를 BigDecimal로 변환
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