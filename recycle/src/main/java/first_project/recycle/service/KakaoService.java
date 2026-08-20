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
        // 카카오 사용자 정보 조회 API에 GET 요청 전송
        ResponseEntity<String> response = rt.exchange(userInfoUrl, HttpMethod.GET, request, String.class);

        try {
            // 사용자 JSON 데이터 파싱
            ObjectMapper om = new ObjectMapper();
            JsonNode root = om.readTree(response.getBody());

            // 카카오 고유 회원 ID 추출
            String providerId = root.get("id").asText();
            JsonNode kakaoAccount = root.path("kakao_account");
            JsonNode profile = kakaoAccount.path("profile");

            // 닉네임 추출 (동의하지 않거나 없을 경우 기본값 세팅)
            String nickname = profile.path("nickname").asText("카카오회원");
            // 이메일 추출 (이메일 권한이 없을 경우 가상 이메일 생성)
            String email = kakaoAccount.path("email").asText(providerId + "@kakao.user");

            // DB에서 기존에 카카오로 가입한 이력이 있는지 확인
            User user = userMapper.findByProviderAndProviderId("KAKAO", providerId);

            // 신규 카카오 가입자라면 DB에 회원 정보 저장
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setName(nickname);
                user.setNickname(nickname);
                user.setProvider("KAKAO");
                user.setProviderId(providerId);
                user.setRole("USER");
                user.setPoint(100);
                user.setNewUser(true);

                userMapper.insertOAuthUser(user);
            }

            // 조회 또는 새로 생성된 회원 객체 반환
            return user;
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패", e);
        }
    }
}