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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    private final UserMapper userMapper;
    private final EcoPointHistoryService ecoPointHistoryService;

    // 객체 재사용을 위한 필드 선언
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

        String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", decodedCode);
        params.add("client_id", clientId.trim());
        params.add("client_secret", clientSecret.trim());
        params.add("redirect_uri", redirectUri.trim());
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (HttpStatusCodeException e) {
            log.error("Google Token API Error Status: {}", e.getStatusCode());
            log.error("Google Token API Error Body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("구글 토큰 발급 실패 (" + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("구글 토큰 파싱 실패", e);
        }
    }

    // 2. Access Token으로 사용자 정보 조회 및 회원가입/로그인
    @Transactional
    public User googleLoginProcess(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            String providerId = root.hasNonNull("id") ? root.get("id").asText() : root.get("sub").asText();
            String name = root.hasNonNull("name") ? root.get("name").asText() : "구글회원";
            String email = root.hasNonNull("email") ? root.get("email").asText() : (providerId + "@google.user");

            User user = userMapper.findByProviderAndProviderId("GOOGLE", providerId);

            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setNickname(name);
                user.setProvider("GOOGLE");
                user.setProviderId(providerId);
                user.setRole("USER");
                user.setNewUser(true);

                userMapper.insertOAuthUser(user);
                ecoPointHistoryService.earnPoint(user.getMemberId(), 100, "SIGNUP", user.getMemberId());
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("구글 사용자 정보 조회 실패", e);
        }
    }
}