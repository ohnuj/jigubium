package first_project.recycle.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecycleApiResponse(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(Header header, Body body) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(Items items){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(List<Item> item){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String itemNm, String dschgMthd){}

}
