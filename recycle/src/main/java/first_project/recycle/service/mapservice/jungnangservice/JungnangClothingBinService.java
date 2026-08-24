package first_project.recycle.service.mapservice.jungnangservice;

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

//일부 좌표 없는 데이터 있어서 카카오 주소 검색 api 사용
@Service
public class JungnangClothingBinService {
    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public JungnangClothingBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. CSV 파일 불러오기
     * 2. 각 행을 ecoLocation으로 변환
     * 3. 없는 좌표는 카카오 주소 검색으로 보완
     * 4. 정상 데이터만 중복 확인 후 DB에 저장
     */
    public List<ecoLocation> importClothingBins() {

        List<ecoLocation> locations = new ArrayList<>();

        // 1. resources/data의 CSV 파일을 CP949로 읽기
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/jungnang-clothing-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 첫 번째 컬럼명 행 건너뛰기
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> data = parseCsvLine(line);

                if (data.size() < 7) {
                    continue;
                }

                // 2. CSV 컬럼 가져오기
                String adminDong = data.get(1).trim();
                String roadAddress = data.get(2).trim();
                String jibunAddress = data.get(3).trim();
                String latitudeValue = data.get(4).trim();
                String longitudeValue = data.get(5).trim();

                // 도로명주소가 없으면 지번주소를 대표 주소로 사용
                String mainAddress = roadAddress;

                if (mainAddress.isEmpty()) {
                    mainAddress = jibunAddress;
                }

                // 두 주소가 모두 없으면 건너뛰기
                if (mainAddress.isEmpty()) {
                    continue;
                }

                ecoLocation location = new ecoLocation();

                location.setLocationName("의류수거함");
                location.setLocationType("의류수거함");

                location.setAdminDong(
                        adminDong.isEmpty() ? null : adminDong
                );

                /*
                 * 중복 확인에 roadAddress를 사용하므로
                 * 도로명주소가 없으면 지번주소를 저장한다.
                 */
                location.setRoadAddress(mainAddress);

                location.setJibunAddress(
                        jibunAddress.isEmpty() ? null : jibunAddress
                );

                // 3. CSV가 제공하는 좌표를 먼저 사용
                location.setLatitude(
                        parseCoordinate(latitudeValue)
                );

                location.setLongitude(
                        parseCoordinate(longitudeValue)
                );

                // 좌표가 없으면 카카오 주소 검색으로 보완
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

                // 주소 검색 후에도 좌표가 없으면 저장하지 않음
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

    // 도로명주소를 먼저 검색하고 실패하면 지번주소 검색
    private void setCoordinatesFromAddress(
            ecoLocation location,
            String roadAddress,
            String jibunAddress
    ) {
        KakaoAddressResponse kakaoResponse =
                searchAddress(roadAddress);

        if (!hasSearchResult(kakaoResponse)) {
            kakaoResponse = searchAddress(jibunAddress);
        }

        if (!hasSearchResult(kakaoResponse)) {
            return;
        }

        KakaoAddressResponse.Document document =
                kakaoResponse.getDocuments().get(0);

        location.setLatitude(
                parseCoordinate(document.getY())
        );

        location.setLongitude(
                parseCoordinate(document.getX())
        );
    }

    // 주소가 있을 때만 카카오 주소 검색 실행
    private KakaoAddressResponse searchAddress(String address) {

        if (address == null || address.isBlank()) {
            return null;
        }

        return kakaoAddressService.searchAddress(address.trim());
    }

    // 카카오 주소 검색 결과가 있는지 확인
    private boolean hasSearchResult(
            KakaoAddressResponse kakaoResponse
    ) {
        return kakaoResponse != null &&
                kakaoResponse.getDocuments() != null &&
                !kakaoResponse.getDocuments().isEmpty();
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
