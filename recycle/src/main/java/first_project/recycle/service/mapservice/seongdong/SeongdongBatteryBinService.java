package first_project.recycle.service.mapservice.seongdong;
import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeongdongBatteryBinService {

    private final KakaoAddressService kakaoAddressService;
    private final EcoLocationMapper ecoLocationMapper;

    public SeongdongBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    public List<EcoLocation> importBatteryBins() {

        List<EcoLocation> savedList = new ArrayList<>();

        ClassPathResource resource =
                new ClassPathResource(
                        "data/seongdong-battery-bin.csv"
                );

        try (
                BufferedReader br = new BufferedReader(
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

                String[] data = line.split(",", -1);

                if (data.length < 5) {
                    continue;
                }

                String adminDong = data[0].trim();
                String roadAddress = data[1].trim();
                String detailLocation = data[2].trim();
                String note = data[4].trim();

                if (roadAddress.isBlank()) {
                    continue;
                }

                String binName =
                        "폐건전지·폐형광등 수거함";

                if (
                        note.contains("폐형광등") &&
                                note.contains("만")
                ) {
                    binName = "폐형광등 수거함";

                } else if (
                        note.contains("폐건전지") &&
                                note.contains("만")
                ) {
                    binName = "폐건전지 수거함";
                }

                String locationName;

                if (!detailLocation.isBlank()) {

                    locationName =
                            detailLocation
                                    + " - "
                                    + binName;

                } else {

                    locationName = binName;
                }

                EcoLocation location =
                        new EcoLocation();

                location.setLocationName(
                        locationName
                );

                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );

                location.setAdminDong(
                        adminDong
                );

                location.setRoadAddress(
                        roadAddress
                );

                location.setJibunAddress(null);

                // 카카오 주소 검색
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                roadAddress
                        );

                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "좌표 변환 실패: "
                                    + roadAddress
                    );

                    continue;
                }

                KakaoAddressResponse.Document document =
                        response
                                .getDocuments()
                                .get(0);

                BigDecimal latitude =
                        new BigDecimal(
                                document.getY()
                        );

                BigDecimal longitude =
                        new BigDecimal(
                                document.getX()
                        );

                location.setLatitude(
                        latitude
                );

                location.setLongitude(
                        longitude
                );

                ecoLocationMapper
                        .insertEcoLocation(
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