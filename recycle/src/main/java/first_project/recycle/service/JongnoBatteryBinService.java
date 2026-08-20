package first_project.recycle.service;

import first_project.recycle.domain.ecoLocationDTO.JongnoBatteryBinResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class JongnoBatteryBinService {

    @Value("${public-data.service-key}")
    private String serviceKey;

    @Value("${public-data.jongno-battery-bin.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    public JongnoBatteryBinResponse getBatteryBins(){
        String url =
                apiUrl
                + "?page=1"
                + "&perPage=1000"
                + "&returnType=JSON";

        return restClient.get()
                .uri(url)
                .header(
                        "Authorization",
                        "Infuser " + serviceKey
                )
                .retrieve()
                .body(JongnoBatteryBinResponse.class);

    }

}
