package first_project.recycle.service.mapservice.gangdongservice;
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
public class GangdongClothingBinService {

    private final EcoLocationMapper ecoLocationMapper;

    public GangdongClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }


    // 강동구 의류수거함 CSV 호출 및 DB 저장
    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/gangdong-clothing-bin.csv"
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

            // CSV 헤더 건너뛰기
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
                 * data[1] = 법정동
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
                        new BigDecimal(latitudeValue);

                BigDecimal longitude =
                        new BigDecimal(longitudeValue);


                EcoLocation location =
                        new EcoLocation();


                // 의류수거함 명칭 / 유형 통일
                location.setLocationName(
                        "의류수거함"
                );

                location.setLocationType(
                        "의류수거함"
                );


                /*
                 * 원본은 "법정동" 컬럼이지만
                 * EcoLocation에는 adminDong 필드가 있으므로
                 * 여기에 저장
                 */
                location.setAdminDong(
                        adminDong
                );


                // 도로명주소 저장
                location.setRoadAddress(
                        roadAddress
                );


                // 지번주소 저장
                location.setJibunAddress(
                        jibunAddress
                );


                // CSV에서 제공된 좌표 그대로 저장
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
