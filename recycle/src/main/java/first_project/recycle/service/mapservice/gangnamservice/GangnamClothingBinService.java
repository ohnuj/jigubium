package first_project.recycle.service.mapservice.gangnamservice;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// cp949인코딩의 csv 좌표를 사용해 강남구 의류수거함 저장
// 위도 경도 제공해주므로 카카오 주소 서비스 사용 x
@Service
public class GangnamClothingBinService {
    private static final Pattern ADMIN_DONG_PATTERN =
            Pattern.compile(
                    "강남구\\s+(.+?동)(?=\\s|\\d|$)"
            );

    private final EcoLocationMapper ecoLocationMapper;

    public GangnamClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }

    // CSV 데이터를 변환하고 DB에 저장
    public List<ecoLocation> importClothingBins() {

        List<ecoLocation> locations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/gangnam-clothing-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 컬럼명 건너뛰기
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> data = parseCsvLine(line);

                if (data.size() < 5) {
                    continue;
                }

                String jibunAddress = data.get(1).trim();
                String roadAddress = data.get(2).trim();
                String latitudeValue = data.get(3).trim();
                String longitudeValue = data.get(4).trim();

                // 도로명주소가 없으면 지번주소 사용
                String mainAddress = roadAddress;

                if (mainAddress.isEmpty()) {
                    mainAddress = jibunAddress;
                }

                if (mainAddress.isEmpty()) {
                    continue;
                }

                BigDecimal latitude =
                        parseCoordinate(latitudeValue);

                BigDecimal longitude =
                        parseCoordinate(longitudeValue);

                // 좌표가 정상적이지 않으면 저장하지 않음
                if (latitude == null || longitude == null) {
                    continue;
                }

                ecoLocation location = new ecoLocation();

                location.setLocationName("의류수거함");
                location.setLocationType("의류수거함");

                location.setAdminDong(
                        extractAdminDong(jibunAddress)
                );

                location.setRoadAddress(mainAddress);

                location.setJibunAddress(
                        jibunAddress.isEmpty()
                                ? null
                                : jibunAddress
                );

                location.setLatitude(latitude);
                location.setLongitude(longitude);

                locations.add(location);

                // 같은 주소와 유형의 데이터인지 확인
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

    // 지번주소에서 동 이름 추출
    private String extractAdminDong(String jibunAddress) {

        if (jibunAddress == null || jibunAddress.isBlank()) {
            return null;
        }

        Matcher matcher =
                ADMIN_DONG_PATTERN.matcher(jibunAddress);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    // 큰따옴표 안의 쉼표는 하나의 값으로 처리
    private List<String> parseCsvLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char currentCharacter = line.charAt(i);

            if (currentCharacter == '"') {

                // 큰따옴표 두 개는 실제 큰따옴표 하나
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
