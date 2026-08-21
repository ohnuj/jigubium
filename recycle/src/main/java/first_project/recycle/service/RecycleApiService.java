package first_project.recycle.service;

import first_project.recycle.dto.RecycleApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

@Service
public class RecycleApiService {

    private final RestClient restClient;
    private final String serviceKey;

    public RecycleApiService(
            @Value("${recycle.api.url}") String apiUrl,
            @Value("${recycle.api.service-key}") String serviceKey) {

        // Spring이 URI 변수 값을 엄격하게 인코딩하도록 설정
        DefaultUriBuilderFactory factory =
                new DefaultUriBuilderFactory(apiUrl);

        factory.setEncodingMode(
                DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES
        );

        this.restClient = RestClient.builder()
                .uriBuilderFactory(factory)
                .build();

        this.serviceKey = serviceKey;
    }


    public List<RecycleApiResponse.Item> searchRecycleApi(String keyword) {

        RecycleApiResponse apiResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("serviceKey", "{serviceKey}")
                        .queryParam("numOfRows", "{numOfRows}")
                        .queryParam("itemNm", "{itemNm}")
                        .build(Map.of(
                                "serviceKey", serviceKey,
                                "numOfRows", 100,
                                "itemNm", keyword
                        )))
                .retrieve()
                .body(RecycleApiResponse.class);


        if (apiResponse == null ||
                apiResponse.response() == null ||
                apiResponse.response().body() == null ||
                apiResponse.response().body().items() == null ||
                apiResponse.response().body().items().item() == null) {

            return List.of();
        }

        return apiResponse.response().body().items().item();
    }
}
