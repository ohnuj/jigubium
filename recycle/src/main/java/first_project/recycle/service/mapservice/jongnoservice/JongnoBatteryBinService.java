package first_project.recycle.service.mapservice.jongnoservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.jongno.JongnoBatteryBinDTO;
import first_project.recycle.domain.ecoLocationdto.jongno.JongnoBatteryBinResponse;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import first_project.recycle.mapper.EcoLocationMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
public class JongnoBatteryBinService {



    // 프로퍼티에 있는 공공데이터 api 인증키 가져오기
    @Value("${public-data.service-key}")
    private String serviceKey;

    // 폐건전지 수거함 공공데이터 api 주소 가져오기
    @Value("${public-data.jongno-battery-bin.url}")
    private String apiUrl;
    //restclient 객체 생성(외부 api 요청 보내는 용)
    private final RestClient restClient = RestClient.create();

    // ecoLocation DB 저장용 Mapper
    private final EcoLocationMapper ecoLocationMapper;

    //카카오맵 연동 주소 검색 서비스(위도/경도 찾아오기)
    private final KakaoAddressService kakaoAddressService;

    // 생성자 주입
    public JongnoBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    // 폐건전지 수거함 데이터 가져와서 리스트에 담기
    public List<EcoLocation> getBatteryBins() {
        //api 요청 주소 형식
        String url =
                apiUrl
                        + "?page=1"
                        + "&perPage=1000"
                        + "&returnType=JSON";

        // api 호출 및 받은 데이터를 객체로 변환
        JongnoBatteryBinResponse response = restClient.get()
                .uri(url)
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(JongnoBatteryBinResponse.class);

        // 수거함 정보를 객체변환 뒤 list로 반환
        List<EcoLocation> locations = response.getData().stream()
                .map(this::convertToEcoLocation)
                .toList();

        // DB에 같은 도로명주소 + 장소타입이 없을 때만 저장
        locations.forEach(location -> {

            if (location.getLatitude() == null || location.getLongitude() == null){
                return;
            }



            int count =
                    ecoLocationMapper.countByRoadAddressAndLocationType(
                            location.getRoadAddress(),
                            location.getLocationType()
                    );

            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }
        });

        return locations;
        }

//        API 데이터
//↓
//카카오 주소검색
//↓
//좌표 있음 → DB 저장 가능
//좌표 없음 → DB 저장 건너뜀
//↓
//전체 locations는 그대로 Controller에 반환
//↓
//프론트에서도 좌표 없는 데이터는 이미 무시
//↓
//나머지 마커는 정상 표시

    //ecoLocation 객체로 변환
    private EcoLocation convertToEcoLocation(JongnoBatteryBinDTO dto) {
        // 변환 결과 저장할 객체
        EcoLocation location = new EcoLocation();

        location.setLocationName("폐건전지·폐형광등 수거함");
        location.setLocationType("폐건전지·폐형광등 수거함");
        location.setAdminDong(dto.getAdminDong());
        location.setRoadAddress(dto.getLocation());

        // 주소를 주소 검색 api에 전달
        KakaoAddressResponse kakaoResponse =
                kakaoAddressService.searchAddress(dto.getLocation());

        //검색 결과가 존재할 때
        if (kakaoResponse.getDocuments() != null
        && kakaoResponse.getDocuments() != null
        && !kakaoResponse.getDocuments().isEmpty()) {
            KakaoAddressResponse.Document document =
                    kakaoResponse.getDocuments().get(0); // 검색 결과중 첫번째 주소 정보 가져오기
            // y = 위도 넣기
            location.setLatitude(
                    new BigDecimal(document.getY())
            );

            // x = 경도 넣기
            location.setLongitude(
                    new BigDecimal(document.getX())
            );
            //지번주소 넣기
            if (document.getAddress() != null) {
                location.setJibunAddress(
                        document.getAddress().getAddressName()
                );
            }

        }

        return location;
    }
}

//종로구 공공데이터 API
//        ↓
//JongnoBatteryBinResponse
//        ↓
//JongnoBatteryBinDTO 한 개씩 꺼냄
//        ↓
//convertToEcoLocation()
//        ↓
//주소를 KakaoAddressService로 전달
//        ↓
//위도 / 경도 / 지번주소 획득
//        ↓
//ecoLocation 완성
//        ↓
//List<ecoLocation> 반환
