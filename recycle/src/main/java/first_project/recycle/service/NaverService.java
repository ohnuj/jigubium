package first_project.recycle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import first_project.recycle.domain.User;
import first_project.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    private final UserMapper userMapper;
    private final EcoPointHistoryService ecoPointHistoryService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    // 1. 네이버 인가 코드와 state로 Access Token 발급 요청
    public String getAccessToken(String code, String state) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId.trim());
        params.add("client_secret", clientSecret.trim());
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("네이버 토큰 발급 실패", e);
            throw new RuntimeException("네이버 토큰 발급 실패", e);
        }
    }

    // 2. Access Token으로 사용자 프로필 조회 및 DB 저장/로그인
    @Transactional
    public User naverLoginProcess(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            // 네이버 응답은 response 객체 내부에 프로필 정보가 위치
            JsonNode naverAccount = root.path("response");

            String providerId = naverAccount.get("id").asText();
            String name = naverAccount.hasNonNull("name") ? naverAccount.get("name").asText() : "네이버회원";
            String nickname = naverAccount.hasNonNull("nickname") ? naverAccount.get("nickname").asText() : name;
            String email = naverAccount.hasNonNull("email") ? naverAccount.get("email").asText() : (providerId + "@naver.user");

            // DB 회원 조회
            User user = userMapper.findByProviderAndProviderId("NAVER", providerId);

            // 신규 가입자 등록 및 에코포인트 100p 지급
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setNickname(nickname);
                user.setProvider("NAVER");
                user.setProviderId(providerId);
                user.setRole("USER");
                user.setNewUser(true);

                userMapper.insertOAuthUser(user);
                ecoPointHistoryService.earnPoint(user.getMemberId(), 100, "SIGNUP", user.getMemberId());
            }

            return user;
        } catch (Exception e) {
            log.error("네이버 사용자 정보 조회 실패", e);
            throw new RuntimeException("네이버 사용자 정보 조회 실패", e);
        }
    }
}