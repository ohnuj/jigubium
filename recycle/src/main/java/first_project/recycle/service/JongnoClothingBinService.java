package first_project.recycle.service;

import first_project.recycle.domain.ecoLocationDTO.JongnoClothingBinDto;
import first_project.recycle.domain.ecoLocationDTO.JongnoClothingBinResponse;
import first_project.recycle.domain.ecoLocation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Service
public class JongnoClothingBinService {

    @Value("${public-data.service-key}")
    private  String serviceKey;

    @Value("${public-data.jongno-clothing-bin.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    public List<ecoLocation> getClothingBins() {
        String url =
                "https://api.odcloud.kr/api/15104622/v1/uddi:2bbbc640-8375-4ea6-b855-e32e4de4b8c5"
                        + "?page=1"
                        + "&perPage=1000"
                        + "&returnType=JSON";
        // api.odcloud.kr 계열의 api에서 인증 시 authorization:infuser 형태 사용

        JongnoClothingBinResponse response = restClient.get()
                .uri(url)
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(JongnoClothingBinResponse.class);

        if (response == null || response.getData() == null) {
            return List.of();
        }
        return response.getData()
                .stream()
                .map(this::convertToEcoLocation)
                .toList();

    }
    // api의 한글로 되어 있는 걸 도메인 속 변수들 속에 넣어줌
    private ecoLocation convertToEcoLocation(JongnoClothingBinDto dto) {

        ecoLocation location = new ecoLocation();

        //locationname 및 type 설정
        location.setLocationName("의류수거함");
        location.setLocationType("의류수거함");

        location.setAdminDong(dto.getAdminDong());
        location.setRoadAddress(dto.getRoadAddress());
        location.setJibunAddress(dto.getJibunAddress());

        //String으로 바꿔논 위도 경도 BigDecimal로 변환
        location.setLatitude(
                new BigDecimal(dto.getLatitude())
        );

        location.setLongitude(
                new BigDecimal(dto.getLongitude())
        );

        return location;
    }


}
