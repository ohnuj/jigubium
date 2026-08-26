package first_project.recycle.service.mapservice.guroservice;
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
public class GuroClothingBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public GuroClothingBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 구로구 의류수거함 CSV 불러오기
     * 2. 생략된 주소에 서울특별시 구로구 추가
     * 3. 카카오 주소 검색으로 좌표 변환
     * 4. 중복되지 않은 정상 데이터만 DB에 저장
     */
    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> locations = new ArrayList<>();

        // 1. resources/data의 CSV 파일을 CP949로 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/guro-clothing-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 첫 번째 컬럼명 행 건너뛰기
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> data = parseCsvLine(line);

                if (data.size() < 4) {
                    continue;
                }

                String adminDong = data.get(1).trim();
                String originalAddress = data.get(2).trim();

                // 주소가 없으면 좌표 검색이 불가능하므로 제외
                if (originalAddress.isEmpty()) {
                    continue;
                }

                // 2. 생략된 지역명을 주소 앞에 추가
                String roadAddress =
                        normalizeAddress(originalAddress);

                EcoLocation location = new EcoLocation();

                location.setLocationName("의류수거함");
                location.setLocationType("의류수거함");

                location.setAdminDong(
                        adminDong.isEmpty() ? null : adminDong
                );

                location.setRoadAddress(roadAddress);

                // 3. 주소를 카카오 API로 검색
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

    // 카카오 검색에 사용할 전체 주소 생성
    private String normalizeAddress(String address) {

        String normalizedAddress =
                address
                        .replace('\u00A0', ' ')
                        .replaceAll("\\s+", " ")
                        .trim();

        if (normalizedAddress.startsWith("서울특별시")) {
            return normalizedAddress;
        }

        if (normalizedAddress.startsWith("서울시")) {
            return normalizedAddress.replaceFirst(
                    "^서울시\\s*",
                    "서울특별시 "
            );
        }

        if (normalizedAddress.startsWith("서울 ")) {
            return normalizedAddress.replaceFirst(
                    "^서울\\s+",
                    "서울특별시 "
            );
        }

        if (normalizedAddress.startsWith("구로구")) {
            return "서울특별시 " + normalizedAddress;
        }

        return "서울특별시 구로구 " + normalizedAddress;
    }

    // 큰따옴표 안의 쉼표를 하나의 컬럼으로 처리
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
