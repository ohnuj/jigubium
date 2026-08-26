package first_project.recycle.service.mapservice.gwanakservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.mapper.EcoLocationMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Service
public class GwanakBatteryBinService {

    private final EcoLocationMapper ecoLocationMapper;

    public GwanakBatteryBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }

    public List<EcoLocation> importBatteryBins() {

        List<EcoLocation> locations = new ArrayList<>();

        try {

            // resources/data 폴더의 CSV 파일
            ClassPathResource resource =
                    new ClassPathResource(
                            "data/gwanak-battery-bin.csv"
                    );

            // CSV는 CP949 인코딩
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    resource.getInputStream(),
                                    Charset.forName("CP949")
                            )
                    );

            Iterable<CSVRecord> records =
                    CSVFormat.DEFAULT
                            .builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .setIgnoreSurroundingSpaces(true)
                            .build()
                            .parse(reader);

            for (CSVRecord record : records) {

                String number = record.get(0).trim();
                String type = record.get(1).trim();
                String roadAddress = record.get(2).trim();
                String jibunAddress = record.get(3).trim();
                String positionName = record.get(4).trim();
                String latitude = record.get(5).trim();
                String longitude = record.get(6).trim();
                String quantity = record.get(7).trim();

                // 좌표가 없으면 지도에 표시할 수 없으므로 제외
                if (
                        latitude.isEmpty() ||
                                longitude.isEmpty()
                ) {
                    continue;
                }

                EcoLocation location =
                        new EcoLocation();

                // 위치명을 화면에 보여주기 좋게 사용
                location.setLocationName(
                        positionName.isEmpty()
                                ? type
                                : positionName
                );

                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );

                // 도로명주소가 비어 있을 수도 있음
                if (!roadAddress.isEmpty()) {
                    location.setRoadAddress(
                            roadAddress
                    );
                }

                if (!jibunAddress.isEmpty()) {
                    location.setJibunAddress(
                            jibunAddress
                    );
                }

                location.setLatitude(
                        new BigDecimal(latitude)
                );

                location.setLongitude(
                        new BigDecimal(longitude)
                );

                locations.add(location);

                /*
                 * 현재는 기존 중복 체크 방식 사용.
                 * 같은 도로명주소에 여러 수거함이 있으면
                 * 하나만 DB에 저장될 수 있음.
                 */
                int count =
                        ecoLocationMapper
                                .countByRoadAddressAndLocationType(
                                        location.getRoadAddress(),
                                        location.getLocationType()
                                );

                if (count == 0) {
                    ecoLocationMapper
                            .insertEcoLocation(location);
                }
            }

            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        System.out.println(
                "관악구 폐건전지·폐형광등 CSV 읽은 개수: "
                        + locations.size()
        );

        return locations;
    }
}