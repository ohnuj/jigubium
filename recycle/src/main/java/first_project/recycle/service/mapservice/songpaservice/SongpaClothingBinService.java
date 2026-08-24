package first_project.recycle.service.mapservice.songpaservice;
import first_project.recycle.domain.ecoLocation;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

// CSV가 좌표를 제공하므로 카카오 주소 검색은 사용하지 않음
@Service
public class SongpaClothingBinService {

    private final EcoLocationMapper ecoLocationMapper;

    public SongpaClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 송파구 의류수거함 CSV 불러오기
     * 2. CSV 데이터를 ecoLocation으로 변환
     * 3. 주소와 좌표가 정상적인 데이터 선별
     * 4. 중복되지 않은 데이터만 DB에 저장
     */
    public List<ecoLocation> importClothingBins() {

        List<ecoLocation> locations = new ArrayList<>();

        // 1. resources/data의 CSV 파일을 CP949로 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/songpa-clothing-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 첫 번째 컬럼명 행 건너뛰기
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> data = parseCsvLine(line);

                if (data.size() < 6) {
                    continue;
                }

                // 2. CSV에서 필요한 값 가져오기
                String adminDong = data.get(2).trim();
                String installationLocation =
                        data.get(3).trim();

                String latitudeValue = data.get(4).trim();
                String longitudeValue = data.get(5).trim();

                // 설치장소가 없으면 저장하지 않음
                if (installationLocation.isEmpty()) {
                    continue;
                }

                String fullAddress =
                        normalizeAddress(installationLocation);

                BigDecimal latitude =
                        parseCoordinate(latitudeValue);

                BigDecimal longitude =
                        parseCoordinate(longitudeValue);

                // 3. 좌표가 정상적이지 않으면 저장하지 않음
                if (latitude == null || longitude == null) {
                    continue;
                }

                ecoLocation location = new ecoLocation();

                location.setLocationName("의류수거함");
                location.setLocationType("의류수거함");

                location.setAdminDong(
                        adminDong.isEmpty() ? null : adminDong
                );

                /*
                 * 설치장소가 지번주소 형식이지만,
                 * 중복 확인을 위해 대표 주소에도 저장한다.
                 */
                location.setRoadAddress(fullAddress);
                location.setJibunAddress(fullAddress);

                location.setLatitude(latitude);
                location.setLongitude(longitude);

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

    // 생략된 서울특별시 송파구 주소를 추가
    private String normalizeAddress(String address) {

        String normalizedAddress = address.trim();

        if (normalizedAddress.startsWith("서울특별시")) {
            return normalizedAddress;
        }

        if (normalizedAddress.startsWith("서울 ")) {
            return normalizedAddress.replaceFirst(
                    "^서울\\s+",
                    "서울특별시 "
            );
        }

        if (normalizedAddress.startsWith("송파구")) {
            return "서울특별시 " + normalizedAddress;
        }

        return "서울특별시 송파구 " + normalizedAddress;
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
