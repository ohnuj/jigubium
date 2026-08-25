package first_project.recycle.service.mapservice.maposervice;
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

// CSV에 좌표가 없어 카카오 주소 검색을 사용
@Service
public class MapoMedicineBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public MapoMedicineBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 마포구 폐의약품 CSV 불러오기
     * 2. CSV 데이터를 ecoLocation으로 변환
     * 3. 세부주소를 카카오 API로 검색
     * 4. 중복되지 않은 정상 데이터만 DB에 저장
     */
    public List<ecoLocation> importMedicineBins() {

        List<ecoLocation> locations = new ArrayList<>();

        // 1. 정리된 CSV 파일을 CP949로 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/mapo-medicine-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 첫 번째 컬럼명 행 건너뛰기
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> data = parseCsvLine(line);

                // 정리된 CSV는 총 7개 컬럼
                if (data.size() < 7) {
                    continue;
                }

                String category = data.get(1).trim();
                String installationName = data.get(2).trim();
                String roadAddress = data.get(3).trim();

                // 주소가 없으면 좌표 검색이 불가능하므로 제외
                if (roadAddress.isEmpty()) {
                    continue;
                }

                ecoLocation location = new ecoLocation();

                /*
                 * 장소명 우선순위
                 * 1. 설치 장소
                 * 2. 구분
                 * 3. 기본 이름
                 */
                String locationName = installationName;

                if (locationName.isEmpty()) {
                    locationName = category;
                }

                if (locationName.isEmpty()) {
                    locationName = "폐의약품 수거함";
                }

                location.setLocationName(locationName);
                location.setLocationType("폐의약품 수거함");
                location.setRoadAddress(roadAddress);

                // 2. 도로명주소를 카카오 API로 검색
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

                // 3. 같은 주소와 장소 유형이 있는지 확인
                int count =
                        ecoLocationMapper
                                .countByRoadAddressAndLocationType(
                                        location.getRoadAddress(),
                                        location.getLocationType()
                                );

                // 4. 중복되지 않은 데이터만 저장
                if (count == 0) {
                    ecoLocationMapper.insertEcoLocation(location);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return locations;
    }

    // 큰따옴표 내부의 쉼표를 하나의 컬럼으로 처리
    private List<String> parseCsvLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char currentCharacter = line.charAt(i);

            if (currentCharacter == '"') {

                if (
                        insideQuotes &&
                                i + 1 < line.length() &&
                                line.charAt(i + 1) == '"'
                ) {
                    currentValue.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (
                    currentCharacter == ',' &&
                            !insideQuotes
            ) {
                values.add(currentValue.toString());
                currentValue.setLength(0);

            } else {
                currentValue.append(currentCharacter);
            }
        }

        values.add(currentValue.toString());

        return values;
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
