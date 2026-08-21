package first_project.recycle.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecycleSearchResponse {
    //recycle_item table
    private Long itemId;
    private String itemName;

    //recycle_category table
    private String categoryName;

    //API
    private String dischargeMethod;

    //recycle_item table
    private String disposalMethod;
    private String feature;
    private String caution;
    // 관련품목 전체
    private String searchKeyword;
    //검색 가능한 관련품목만
    private List<String> relatedItems;
}
