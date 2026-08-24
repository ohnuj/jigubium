package first_project.recycle.service.GameService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GameApiService {

    @Value("${recycle.api.service-key}")
    private String serviceKey;

    @Value("${recycle.api.url}")
    private String apiUrl;

    private static final List<String> KEYWORDS = List.of(
            "생수", "페트", "상자", "신문", "알루미늄", "통조림", "유리병", "플라스틱", "종이컵", "스티로폼"
    );

    public List<Map<String, String>> getRandomGameItems(int count) {
        // RestTemplate 내부 자동 인코딩 비활성화
        RestTemplate restTemplate = new RestTemplate();
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(factory);

        List<Map<String, String>> resultList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        List<String> shuffledKeywords = new ArrayList<>(KEYWORDS);
        Collections.shuffle(shuffledKeywords);

        for (String keyword : shuffledKeywords) {
            if (resultList.size() >= count) break;

            try {
                // serviceKey(Decoding키) 및 한글 검색어를 각각 UTF-8 인코딩
                String encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
                String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

                String fullUrl = apiUrl + "/getItem"
                        + "?serviceKey=" + encodedKey
                        + "&pageNo=1"
                        + "&numOfRows=10"
                        + "&_type=json"
                        + "&itemNm=" + encodedKeyword;

                URI uri = URI.create(fullUrl);

                String jsonResponse = restTemplate.getForObject(uri, String.class);
                JsonNode root = objectMapper.readTree(jsonResponse);
                JsonNode items = root.path("response").path("body").path("items").path("item");

                if (items.isArray()) {
                    for (JsonNode itemNode : items) {
                        String name = itemNode.path("itemNm").asText();
                        String dschgMthd = itemNode.path("dschgMthd").asText();
                        String category = convertCategory(name, dschgMthd);

                        if (!"general".equals(category) && resultList.size() < count) {
                            resultList.add(Map.of("name", name, "category", category));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("API 호출 에러 (키워드: " + keyword + ") : " + e.getMessage());
            }
        }

        if (resultList.size() < count) {
            resultList = List.of(
                    Map.of("name", "생수 페트병", "category", "plastic"),
                    Map.of("name", "택배 박스", "category", "paper"),
                    Map.of("name", "음료수 캔", "category", "can")
            );
        }

        return resultList;
    }

    private String convertCategory(String name, String method) {
        String text = name + " " + method;
        if (text.contains("플라스틱") || text.contains("페트")) return "plastic";
        if (text.contains("종이") || text.contains("박스") || text.contains("신문")) return "paper";
        if (text.contains("캔") || text.contains("고철") || text.contains("알루미늄")) return "can";
        if (text.contains("유리")) return "glass";
        return "general";
    }
}