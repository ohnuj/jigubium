package first_project.recycle.service.mapservice.seongbukservice;

import first_project.recycle.domain.EcoLocation;
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

/**
 * CSV 불러오기
 * → 한 줄씩 읽기
 * → 주소와 좌표 추출
 * → 동 이름 추출
 * → ecoLocation으로 변환
 * → 중복 확인
 * → DB 저장
 * → JSON 목록 반환
 */
// CSV 좌표를 사용해 성북구 의류수거함 저장
@Service
public class SeongbukClothingBinService {

    // 지번주소에서 동 이름을 추출할 정규표현식
    private static final Pattern ADMIN_DONG_PATTERN =
            Pattern.compile(
                    "성북구\\s+(.+?동)(?=\\s|\\d|$)"
            );

    private final EcoLocationMapper ecoLocationMapper;

    public SeongbukClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }

    // CSV 데이터를 변환하고 DB에 저장
    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> locations = new ArrayList<>();

        /*
         * 1. resources/data 폴더의 CSV 파일 불러오기
         * 2. 한글이 깨지지 않도록 CP949 인코딩으로 읽기
         */
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/seongbuk-clothing-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 3. 첫 번째 줄인 컬럼명 건너뛰기
            reader.readLine();

            String line;

            // 4. CSV 파일을 마지막 줄까지 한 줄씩 읽기
            while ((line = reader.readLine()) != null) {

                /*
                 * 5. 한 줄을 쉼표 기준으로 분리
                 * -1을 사용하면 빈 컬럼도 배열에 포함된다.
                 */
                String[] data = line.split(",", -1);

                // 정상적인 데이터는 컬럼이 6개여야 함
                if (data.length < 6) {
                    continue;
                }

                // 6. 필요한 주소와 좌표 값 꺼내기
                String roadAddress = data[2].trim();
                String jibunAddress = data[3].trim();
                String longitudeValue = data[4].trim();
                String latitudeValue = data[5].trim();

                // 도로명주소가 없으면 저장하지 않음
                if (roadAddress.isEmpty()) {
                    continue;
                }

                // 7. 문자열 좌표를 BigDecimal로 변환
                BigDecimal longitude =
                        parseCoordinate(longitudeValue);

                BigDecimal latitude =
                        parseCoordinate(latitudeValue);

                // 좌표가 없거나 숫자가 아니면 저장하지 않음
                if (latitude == null || longitude == null) {
                    continue;
                }

                // 8. CSV 데이터를 ecoLocation 객체로 변환
                EcoLocation location = new EcoLocation();

                location.setLocationName("의류수거함");
                location.setLocationType("의류수거함");
                location.setRoadAddress(roadAddress);

                location.setJibunAddress(
                        jibunAddress.isEmpty()
                                ? null
                                : jibunAddress
                );

                location.setLatitude(latitude);
                location.setLongitude(longitude);

                // 9. 지번주소에서 동 이름을 추출해 저장
                location.setAdminDong(
                        extractAdminDong(jibunAddress)
                );

                // 10. JSON으로 반환할 목록에 추가
                locations.add(location);

                // 11. 같은 주소와 유형의 데이터가 있는지 확인
                int count =
                        ecoLocationMapper
                                .countByRoadAddressAndLocationType(
                                        location.getRoadAddress(),
                                        location.getLocationType()
                                );

                // 12. 중복되지 않은 데이터만 DB에 저장
                if (count == 0) {
                    ecoLocationMapper.insertEcoLocation(location);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 13. 변환된 의류수거함 목록 반환
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