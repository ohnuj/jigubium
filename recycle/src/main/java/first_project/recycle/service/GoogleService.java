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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    private final UserMapper userMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.client-secret}")
    private String clientSecret;

    // 1. 구글 인가 코드로 Access Token 요청
    public String getAccessToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId.trim());
        params.add("client_secret", clientSecret.trim());
        params.add("redirect_uri", redirectUri.trim());
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode accessToken = jsonNode.get("access_token");

            if (accessToken == null || accessToken.asText().isBlank()) {
                throw new IllegalStateException("구글 ACCESS TOKEN을 발급받지 못했습니다.");
            }
            return accessToken.asText();
        } catch (HttpStatusCodeException e) {
            log.error("Google Token API 요청 실패. status={}", e.getStatusCode());
            throw new RuntimeException("구글 토큰 발급에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("구글 토큰 응답 처리 실패", e);
            throw new RuntimeException("구글 토큰 응답 처리에 실패하였습니다.", e);
        }
    }

    // 2. Access Token으로 사용자 정보 조회 및 신규/기존 회원 판별
    public User googleLoginProcess(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode idNode = root.get("id");

            if (idNode == null || idNode.asText().isBlank()) {
                throw new IllegalStateException(
                        "구글 회원 식별자를 조회하지 못했습니다."
                );
            }

            String providerId = idNode.asText();
            String name = root.hasNonNull("name") ? root.get("name").asText() : "구글회원";
            String email = root.hasNonNull("email") ? root.get("email").asText() : (providerId + "@google.user");

            // DB 회원 조회
            User user = userMapper.findByProviderAndProviderId("GOOGLE", providerId);

            // 신규 회원인 경우: DB 저장 없이 기본 정보만 담은 임시 객체 반환
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setNickname(name); // 폼 초기값으로 사용할 닉네임
                user.setProvider("GOOGLE");
                user.setProviderId(providerId);
                user.setRole("USER");
                user.setNewUser(true); // 신규 가입 플래그 설정
            } else {
                user.setNewUser(false); // 기존 회원 플래그 설정
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("구글 사용자 정보 조회 실패", e);
        }
    }
}