package first_project.recycle.service.mapservice.seongdong;

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
public class SeongdongClothingBinService {

    private final EcoLocationMapper ecoLocationMapper;

    public SeongdongClothingBinService(EcoLocationMapper ecoLocationMapper) {
        this.ecoLocationMapper = ecoLocationMapper;
    }

    public List<EcoLocation> importClothingBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource("data/seongdong-clothing-bin.csv");

        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                resource.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {

            // CSV 첫 번째 줄(헤더) 건너뛰기
            br.readLine();

            String line;

            while ((line = br.readLine()) != null) {

                // 빈 줄이면 건너뛰기
                if (line.isBlank()) {
                    continue;
                }

                String[] data = line.split(",", -1);

                // 필요한 컬럼이 부족하면 건너뛰기
                if (data.length < 6) {
                    continue;
                }

                String adminDong = data[2].trim();
                String roadAddress = data[3].trim();
                String longitudeText = data[4].trim();
                String latitudeText = data[5].trim();

                // 주소 또는 좌표가 없는 데이터는 저장하지 않음
                if (
                        roadAddress.isBlank() ||
                                longitudeText.isBlank() ||
                                latitudeText.isBlank()
                ) {
                    continue;
                }

                BigDecimal longitude =
                        new BigDecimal(longitudeText);

                BigDecimal latitude =
                        new BigDecimal(latitudeText);

                EcoLocation location = new EcoLocation();

                location.setLocationName("의류수거함");
                location.setLocationType("의류수거함");
                location.setAdminDong(adminDong);
                location.setRoadAddress(roadAddress);
                location.setJibunAddress(null);
                location.setLatitude(latitude);
                location.setLongitude(longitude);

                // DB 저장
                ecoLocationMapper.insertEcoLocation(location);

                // 저장된 데이터 반환용
                savedList.add(location);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedList;
    }
}