package first_project.recycle.domain;

import lombok.Data;

@Data
public class RecycleItem {
    private Long itemId;
    private Long sourceItemId;
    private String itemName;
    private String disposalMethod;
    private String feature;
    private String caution;
    private String searchKeyword;



}
