package first_project.recycle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import first_project.recycle.domain.User;
import first_project.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// 스프링 서비스 빈 등록
@Service
// final 필드(userMapper)에 대한 생성자 자동 생성(DI)
@RequiredArgsConstructor
public class KakaoService {

    private final UserMapper userMapper;
    private final EcoPointHistoryService ecoPointHistoryService;

    // application.properties에 설정된 카카오 REST API 키 주입
    @Value("${kakao.client-id}")
    private String clientId;

    // application.properties에 설정된 리다이렉트 URI 주입
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    // application.properties에 설정된 카카오 Client Secret 주입
    @Value("${kakao.client-secret}")
    private String clientSecret;

    // 카카오 인가 코드를 받아 카카오 인증 서버로부터 Access Token을 발급받는 메서드
    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        RestTemplate rt = new RestTemplate();

        // HTTP Header 설정 (Form URL Encoded 형식)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // HTTP Body에 필수 요청 파라미터 구성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);
        params.add("client_secret", clientSecret);

        // Header와 Body를 결합한 HttpEntity 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 카카오 토큰 발급 API에 POST 요청 전송
        ResponseEntity<String> response = rt.postForEntity(tokenUrl, request, String.class);

        try {
            // JSON 응답 데이터 파싱하여 access_token 추출
            ObjectMapper om = new ObjectMapper();
            JsonNode jsonNode = om.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰 파싱 실패", e);
        }
    }

    // Access Token으로 사용자 정보를 조회하고 DB 저장/조회 후 User 객체를 반환하는 메서드
    @Transactional
    public User kakaoLoginProcess(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        RestTemplate rt = new RestTemplate();

        // 발급받은 Access Token을 Bearer 인증 헤더에 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = rt.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        try {
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(response.getBody());

            String providerId = root.get("id").asText();
            JsonNode kakaoAccount = root.path("kakao_account");
            JsonNode profile = kakaoAccount.path("profile");

            String nickname = profile.path("nickname").asText("카카오회원");
            String email = kakaoAccount.path("email").asText(providerId + "@kakao.user");

            // DB에서 기존 가입 이력 확인
            User user = userMapper.findByProviderAndProviderId("KAKAO", providerId);

            // 신규 회원인 경우: DB에 저장하지 않고 기본 정보만 담은 임시 객체 생성
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setName(nickname);
                user.setNickname(nickname); // 가입 폼 초기값으로 사용할 기본 닉네임
                user.setProvider("KAKAO");
                user.setProviderId(providerId);
                user.setRole("USER");
                user.setNewUser(true); // 신규 가입자 플래그 설정
            } else {
                user.setNewUser(false); // 기존 회원 플래그 설정
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }
}