package first_project.recycle.service;

import first_project.recycle.domain.ecoLocationDTO.KakaoAddressResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class KakaoAddressService {
    @Value("${kakao.rest-api-key}") // 프로퍼티스에서 카카오 RestApi 키 가져오기
    private String restApiKey;

    // restclient 객체 생성
    private final RestClient restClient = RestClient.create();

    // 카카오 주소 검색 api에 요청
    public KakaoAddressResponse searchAddress(String address){

        //카카오 주소 검색 api의 주소 구성
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/search/address.json")
                        .queryParam("query", address) // 검색할 주소를 쿼리 파라미터로 전달
                        .build())
                //카카오 rest api인증키를 헤더에 담아서 전달하기
                .header(
                        "Authorization",
                        "KakaoAK " + restApiKey
                )
                // api 요청 전송 후 응답받기
                .retrieve()
                // 받은 데이터를 객체로 변환
                .body(KakaoAddressResponse.class);
    }
}


//"서울 종로구 자하문로 92"
//        ↓
//searchAddress(address)
//        ↓
//카카오 주소검색 API 요청
//        ↓
//JSON 응답
//        ↓
//KakaoAddressResponse로 변환
//        ↓
//x = 경도
//y = 위도
//address = 지번주소 정보