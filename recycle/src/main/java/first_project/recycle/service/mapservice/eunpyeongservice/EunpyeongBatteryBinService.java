package first_project.recycle.service.mapservice.eunpyeongservice;
import first_project.recycle.domain.EcoLocation;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class EunpyeongBatteryBinService {

    private final EcoLocationMapper ecoLocationMapper;

    public EunpyeongBatteryBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 은평구 폐건전지·폐형광등 CSV 호출 및 DB 저장
    public List<EcoLocation> importBatteryBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/eunpyeong-battery-bin.csv"
                );


        try (
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            // 헤더 건너뛰기
            br.readLine();

            String line;

            while ((line = br.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }


                /*
                 * CSV 구조
                 *
                 * data[0] = 위치정보
                 * data[1] = 소재지도로명주소
                 * data[2] = 위도
                 * data[3] = 경도
                 */
                String[] data =
                        line.split(",", -1);

                if (data.length < 4) {
                    continue;
                }


                String locationName =
                        data[0].trim();

                String roadAddress =
                        data[1].trim();

                String latitudeValue =
                        data[2].trim();

                String longitudeValue =
                        data[3].trim();


                /*
                 * 좌표가 없으면 지도에 표시할 수 없으므로 제외
                 */
                if (
                        latitudeValue.isBlank() ||
                                longitudeValue.isBlank()
                ) {
                    continue;
                }


                BigDecimal latitude =
                        new BigDecimal(
                                latitudeValue
                        );

                BigDecimal longitude =
                        new BigDecimal(
                                longitudeValue
                        );


                EcoLocation location =
                        new EcoLocation();


                /*
                 * 실제 위치정보가 있으므로
                 * locationName에 그대로 저장
                 */
                if (locationName.isBlank()) {

                    location.setLocationName(
                            "폐건전지·폐형광등 수거함"
                    );

                } else {

                    location.setLocationName(
                            locationName
                    );
                }


                // 프로젝트에서 사용하는 장소 유형으로 통일
                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );


                /*
                 * CSV에 행정동 컬럼이 없으므로
                 * 임의로 추측하지 않고 null 저장
                 */
                location.setAdminDong(
                        null
                );


                // 정리한 도로명주소 저장
                location.setRoadAddress(
                        roadAddress
                );


                // 별도 지번주소는 없음
                location.setJibunAddress(
                        null
                );


                // CSV에 있는 좌표 그대로 저장
                location.setLatitude(
                        latitude
                );

                location.setLongitude(
                        longitude
                );


                // DB 저장
                ecoLocationMapper.insertEcoLocation(
                        location
                );


                savedList.add(
                        location
                );
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return savedList;
    }
}
