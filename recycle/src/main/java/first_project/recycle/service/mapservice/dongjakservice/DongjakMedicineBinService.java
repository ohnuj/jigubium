package first_project.recycle.service.mapservice.dongjakservice;


import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.domain.ecoLocationdto.dongjak.DongjakMedicineBinResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

// api에 위도 경도 제공 안해서 KakaoAddressService로 주소 받아야 함
@Service
public class DongjakMedicineBinService {
    @Value("${PUBLIC_DATA_SERVICE_KEY}")
    private String serviceKey;
    private final RestClient restClient;

    //주소를 카카오 api에 전달해 위도 경도 받아오는 서비스
    private final KakaoAddressService kakaoAddressService;

    // 폐의약품 수거 장소를 DB에 저장하고 중복여부 확인하는 mapper
    private final EcoLocationMapper ecoLocationMapper;

    // 생성자 주입
    public DongjakMedicineBinService(KakaoAddressService kakaoAddressService,
                                     EcoLocationMapper ecoLocationMapper)
    {
        this.restClient = RestClient.create();
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }

    /**
     * 1. 동작구 공공데이터 API 호출
     * 2. JSON 응답을 DongjakMedicineBinResponse로 변환
     * 3. 약국 정보를 ecoLocation으로 변환
     * 4. 약국 주소를 카카오 API로 검색하여 좌표 변환
     * 5. 주소와 좌표가 정상적인 데이터만 선별
     * 6. 기존 DB 데이터와 중복되는지 확인
     * 7. 중복되지 않은 데이터만 DB에 저장
     */
    public List<EcoLocation> getMedicineBins(){
        String url = "https://api.odcloud.kr/api/15077702/v1/"
                + "uddi:222c1714-3518-4070-a74a-b7abc7304bb6"
                + "?page=1&perPage=1000";
        //  Authorization 헤더에 서비스 키를 넣어 GET 요청을 보낸다.
        DongjakMedicineBinResponse response = restClient
                .get().uri(url).header("Authorization", "Infuser " + serviceKey)
                .retrieve().body(DongjakMedicineBinResponse.class);
        // api 응답없거나 data가 없을 시 nullpointerexception 방지
        if (response == null || response.getData() == null){
            return List.of();
        }
        // 데이터를 ecoLocation 목록으로 변환 작업
        List<EcoLocation> locations = response.getData().stream().map(this::convertToEcoLocation).toList();
        // 변환된 데이터들을 검사해 정상적인 것만 DB에 저장
        for (EcoLocation location : locations){
            if (location.getRoadAddress() == null || location.getRoadAddress().isBlank() ||
            location.getLatitude() == null || location.getLongitude() == null){
                continue;
            }
            // 중복인지 체크
            int count = ecoLocationMapper.countByRoadAddressAndLocationType(location.getRoadAddress(),
                    location.getLocationType());
            if (count == 0) {
                ecoLocationMapper.insertEcoLocation(location);
            }// count가 0이면 중복되지 않는단 뜻으로 DB에 저장
        }
        return locations;

    }
    private EcoLocation convertToEcoLocation(
            DongjakMedicineBinResponse.DataItem dataItem
    ){
        EcoLocation location = new EcoLocation();
        // 약국 명칭을 그대로 사용하되 만약 명칭이 없다면 기본 이름 쓰기
        String locationName = dataItem.getPharmacyName();
        if (locationName == null || locationName.isBlank()) {
            locationName = "폐의약품 수거 가능 약국";
        }
        location.setLocationName(locationName);
        location.setLocationType("폐의약품 수거함");
        // 단일 주소만 제공하므로 도로명 주소만 채우고 지번 주소는 비워둠
        String originalAddress = dataItem.getAddress();
        location.setRoadAddress(originalAddress);
        // 주소가 없는 데이터는 제외시키기 위해 일단 그대로 둔다
        if (originalAddress == null || originalAddress.isBlank()){
            return location;
        }
        // 층수, 호수 등 상세 주소는 제외시킨다.
        String searchAddress = cleanAddress(originalAddress);
        // 카카오 주소 검색 api에 전달
        KakaoAddressResponse kakaoResponse = kakaoAddressService.searchAddress(searchAddress);
        // 카카오 api가 정상적으로 반환해주면 위도 경도 저장
        if (kakaoResponse != null && kakaoResponse.getDocuments() != null &&
        !kakaoResponse.getDocuments().isEmpty()){
            KakaoAddressResponse.Document document = kakaoResponse.getDocuments().get(0);

            location.setLatitude(
                    parseCoordinate(document.getY())
            );
            location.setLongitude(
                    parseCoordinate(document.getX())
            );
        }
        return location;
    }
    // 상세 주소 제거하는 메서드(깔끔한 주소를 카카오 api에 전달하기 위해)
    private String cleanAddress(String address) {
        String cleanedAddress = address.trim();
        // 상세 주소는 보통 주소 끝에 , 붙여서 처리하므로 , 뒤의 내용을 제거하면 된다
        int commaIndex = cleanedAddress.indexOf(",");
        if (commaIndex >= 0) {
            cleanedAddress =
                    cleanedAddress.substring(0, commaIndex);
        }
        // 괄호도 제거 ex. 서울특별시 동작구 서달로 157 (흑석동) <- 이거
        cleanedAddress  = cleanedAddress.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
        return cleanedAddress;

    }
    // 데이터를 BigDecimal로 변환
    private BigDecimal parseCoordinate(String coordinate){
        // 좌표 없을 시 그냥 null로 반환
        if (coordinate == null || coordinate.isBlank()){
            return  null;
        }
        try {
            return new BigDecimal(coordinate.trim());
        }catch (NumberFormatException e){
            // 숫자 외의 좌표가 들어올 시 api 오류 방지 위해 null 반환
            return null;
        }
    }
}
