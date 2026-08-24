package first_project.recycle.service.mapservice.gangnamservice;

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

// csv에 좌표가 없기때문에 카카오 api로 변화해 가져와야 함
@Service
public class GangnamBatteryBinService {
    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public GangnamBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    // CSV 데이터를 변환하고 DB에 저장
    public List<ecoLocation> importBatteryBins() {

        List<ecoLocation> locations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource(
                                "data/gangnam-battery-bin.csv"
                        ).getInputStream(),
                        Charset.forName("CP949")
                )
        )) {
            // 컬럼명 건너뛰기
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                List<String> data = parseCsvLine(line);

                if (data.size() < 6) {
                    continue;
                }

                String adminDong = data.get(0).trim();
                String originalAddress = data.get(1).trim();


                if (originalAddress.isEmpty()) {
                    continue;
                }

                ecoLocation location = new ecoLocation();

                location.setLocationName(originalAddress);

                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );

                location.setAdminDong(
                        adminDong.isEmpty() ? null : adminDong
                );

                // DB에는 상세주소가 포함된 원본 주소 저장
                location.setRoadAddress(originalAddress);

                // 카카오 검색에는 정리한 주소 사용
                String searchAddress =
                        cleanAddress(originalAddress);

                KakaoAddressResponse kakaoResponse =
                        kakaoAddressService.searchAddress(
                                searchAddress
                        );

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

                // 같은 주소와 장소 유형인지 확인
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



    // 상세주소와 괄호를 제거해 카카오 검색용 주소 생성
    private String cleanAddress(String address) {

        String cleanedAddress = address.trim();

        int commaIndex = cleanedAddress.indexOf(",");

        if (commaIndex >= 0) {
            cleanedAddress =
                    cleanedAddress.substring(0, commaIndex);
        }

        cleanedAddress =
                cleanedAddress
                        .replaceAll(
                                "\\s*\\([^)]*\\)\\s*$",
                                ""
                        )
                        .trim();

        // 도로명과 건물번호 사이에 공백이 없는 주소 보정
        cleanedAddress =
                cleanedAddress.replaceFirst(
                        "(?<=[로길])(\\d+(?:-\\d+)?)$",
                        " $1"
                );

        return cleanedAddress;
    }

    // 큰따옴표 안의 쉼표는 하나의 값으로 처리
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
