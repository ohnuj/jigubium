package first_project.recycle.service.mapservice.seodaemunservice;



import first_project.recycle.domain.ecoLocationdto.seodaemun.SeodaemunMedicineBinResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SeodaemunMedicineBinService {

    private final WebClient webClient;

    @Value("${public.data.service-key}")
    private String serviceKey;

    public SeodaemunMedicineBinService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public SeodaemunMedicineBinResponse getMedicineBins() {

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.odcloud.kr")
                        .path("/api/15074855/v1/uddi:44288d18-214b-41a0-a4af-d693fd4d1bc0")
                        .queryParam("page", 1)
                        .queryParam("perPage", 100)
                        .queryParam("serviceKey", serviceKey)
                        .build()
                )
                .retrieve()
                .bodyToMono(SeodaemunMedicineBinResponse.class)
                .block();
    }
}
