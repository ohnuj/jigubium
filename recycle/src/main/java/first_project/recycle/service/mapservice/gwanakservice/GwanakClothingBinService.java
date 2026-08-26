package first_project.recycle.service.mapservice.gwanakservice;

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

@Service
public class GwanakClothingBinService {

    private final EcoLocationMapper ecoLocationMapper;

    public GwanakClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }

    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> locations = new ArrayList<>();

        try {

            // resources/data 폴더의 CSV 파일 가져오기
            ClassPathResource resource =
                    new ClassPathResource(
                            "data/gwanak-clothing-bin.csv"
                    );

            // 관악구 CSV 파일은 CP949 인코딩
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    resource.getInputStream(),
                                    Charset.forName("CP949")
                            )
                    );

            String line;

            // 첫 번째 줄(컬럼명) 건너뛰기
            reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",", -1);

                // 컬럼이 정상적으로 4개가 아니면 건너뛰기
                if (data.length < 4) {
                    continue;
                }

                String binName = data[0].trim();
                String roadAddress = data[1].trim();
                String latitude = data[2].trim();
                String longitude = data[3].trim();

                // 주소 또는 좌표가 없으면 저장하지 않음
                if (
                        roadAddress.isEmpty() ||
                                latitude.isEmpty() ||
                                longitude.isEmpty()
                ) {
                    continue;
                }

                EcoLocation location = new EcoLocation();

                // 예: 낙성대동-1
                location.setLocationName(binName);

                location.setLocationType("의류수거함");

                // 낙성대동-1 → 낙성대동
                if (binName.contains("-")) {
                    location.setAdminDong(
                            binName.substring(
                                    0,
                                    binName.lastIndexOf("-")
                            )
                    );
                }

                location.setRoadAddress(roadAddress);

                location.setLatitude(
                        new BigDecimal(latitude)
                );

                location.setLongitude(
                        new BigDecimal(longitude)
                );

                locations.add(location);

                // 같은 주소 + 같은 종류가 DB에 있는지 확인
                int count =
                        ecoLocationMapper
                                .countByRoadAddressAndLocationType(
                                        location.getRoadAddress(),
                                        location.getLocationType()
                                );

                // 없는 데이터만 저장
                if (count == 0) {
                    ecoLocationMapper.insertEcoLocation(location);
                }
            }

            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return locations;
    }
}
