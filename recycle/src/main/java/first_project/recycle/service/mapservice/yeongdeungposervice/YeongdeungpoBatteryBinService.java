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

// CSV인코딩은 CP949이며 좌표가 없어 카카오 주소 검색을 사용
@Service
public class YeongdeungpoBatteryBinService {
    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public YeongdeungpoBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    //좌표 변환하고 DB에 저장
    public List<ecoLocation> importBatteryBins() {
        List<ecoLocation> locations = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("data/yeongdeungpo-battery-bin.csv")
                        .getInputStream(), Charset.forName("CP949")
                )
        )
        ) {// 첫번째 컬럼명 줄 건너뛰기
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> data = parseCsvLine(line);

                if (data.size() < 7) {
                    continue;
                }
                String adminDong = data.get(1).trim();
                String originalAddress = data.get(2).trim();
                String category = data.get(3).trim();
                String detailName = data.get(4).trim();
                //주소 없을 시 좌표 검색 불가
                if (originalAddress.isEmpty()) {
                    continue;
                }
                String roadAddress = normalizeAddress(originalAddress);
                ecoLocation location = new ecoLocation();

                // 세부위치를 장소명으로 사용
                String locationName = detailName;
                if (locationName.isEmpty()) {
                    if (category.isEmpty()) {
                        locationName = "폐건전지·폐형광등 수거함";
                    } else {
                        locationName = category + " 폐건전지·폐형광등 수거함";
                    }
                }
                location.setLocationName(locationName);
                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );

                location.setAdminDong(
                        adminDong.isEmpty() ? null : adminDong
                );
                location.setRoadAddress(roadAddress);
                //주소를 위도 경도로 변환
                KakaoAddressResponse kakaoResponse = kakaoAddressService.searchAddress(roadAddress);
                if (hasSearchResult(kakaoResponse)) {
                    KakaoAddressResponse.Document document = kakaoResponse
                            .getDocuments()
                            .get(0);
                    location.setLatitude(
                            parseCoordinate(document.getY())
                    );

                    location.setLongitude(
                            parseCoordinate(document.getX())
                    );
                }
                //좌표 검색 실패 데이터는 저장 x
                if (location.getLatitude() == null || location.getLongitude() == null) {
                    continue;
                }
                locations.add(location);

                // 같은 주소와 장소 유형이 있는지 확인
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

    /**
     * 큰따옴표 안의 쉼표를 하나의 값으로 처리한다.
     */
    private List<String> parseCsvLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char currentCharacter = line.charAt(i);

            if (currentCharacter == '"') {

                // 큰따옴표 두 개는 실제 큰따옴표 하나를 의미
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

        // 마지막 컬럼 추가
        values.add(currentValue.toString());

        return values;
    }

    /**
     * 카카오 검색에 사용할 주소 형식으로 정리한다.
     */
    private String normalizeAddress(String address) {

        String normalizedAddress = address.trim();

        if (normalizedAddress.startsWith("서울특별시")) {
            return normalizedAddress;
        }

        if (normalizedAddress.startsWith("서울 ")) {
            normalizedAddress =
                    normalizedAddress.replaceFirst(
                            "^서울\\s+",
                            "서울특별시 "
                    );

            return normalizedAddress;
        }

        if (normalizedAddress.startsWith("영등포구")) {
            normalizedAddress =
                    "서울특별시 " + normalizedAddress;

        } else {
            normalizedAddress =
                    "서울특별시 영등포구 "
                            + normalizedAddress;
        }

        // 건물번호 앞 공백이 없는 일부 주소 보정
        normalizedAddress =
                normalizedAddress.replaceFirst(
                        "(?<=[로길])(\\d+(?:-\\d+)?)$",
                        " $1"
                );

        return normalizedAddress;
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











