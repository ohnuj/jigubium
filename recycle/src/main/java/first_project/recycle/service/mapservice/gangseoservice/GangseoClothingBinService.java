package first_project.recycle.service.mapservice.gangseoservice;
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
public class GangseoClothingBinService {

    private final EcoLocationMapper ecoLocationMapper;

    public GangseoClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 강서구 의류수거함 CSV 호출 및 DB 저장
    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/gangseo-clothing-bin.csv"
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

            // 첫 번째 헤더 행 건너뛰기
            br.readLine();

            String line;

            while ((line = br.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }


                /*
                 * CSV 구조
                 *
                 * data[0] = 연번
                 * data[1] = 행정동
                 * data[2] = 도로명주소
                 * data[3] = 지번주소
                 * data[4] = 위도
                 * data[5] = 경도
                 */
                String[] data =
                        line.split(",", -1);

                if (data.length < 6) {
                    continue;
                }


                String adminDong =
                        data[1].trim();

                String roadAddress =
                        data[2].trim();

                String jibunAddress =
                        data[3].trim();

                String latitudeValue =
                        data[4].trim();

                String longitudeValue =
                        data[5].trim();


                // 좌표가 없으면 지도에 표시할 수 없으므로 제외
                if (
                        latitudeValue.isBlank() ||
                                longitudeValue.isBlank()
                ) {
                    continue;
                }


                BigDecimal latitude =
                        new BigDecimal(latitudeValue);

                BigDecimal longitude =
                        new BigDecimal(longitudeValue);


                EcoLocation location =
                        new EcoLocation();


                // 의류수거함 명칭 및 유형 통일
                location.setLocationName(
                        "의류수거함"
                );

                location.setLocationType(
                        "의류수거함"
                );


                // 행정동 저장
                location.setAdminDong(
                        adminDong
                );


                /*
                 * 일부 데이터는 도로명주소가 없으므로
                 * 빈 값이면 null로 저장
                 */
                if (roadAddress.isBlank()) {

                    location.setRoadAddress(
                            null
                    );

                } else {

                    location.setRoadAddress(
                            roadAddress
                    );
                }


                /*
                 * 지번주소는 모든 데이터에 존재하지만
                 * 혹시 빈 값이 있을 경우를 대비
                 */
                if (jibunAddress.isBlank()) {

                    location.setJibunAddress(
                            null
                    );

                } else {

                    location.setJibunAddress(
                            jibunAddress
                    );
                }


                // CSV에 제공된 좌표 그대로 저장
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
