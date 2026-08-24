package first_project.recycle.repository;

import first_project.recycle.domain.Paging;
import first_project.recycle.domain.RecycleItem;
import first_project.recycle.dto.RecycleSearchResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecycleMapper {
    //for User
    List<RecycleSearchResponse> searchRecycleItems(@Param("keyword") String keyword);
    //for User
    List<String> findExistingItemNames(@Param("itemNames") List<String> itemNames);

    //관리자용 > 재활용 품목 전체 조회
    //검색 안하면 전체 조회, 키워드 입력하면 부분조회
    List<RecycleSearchResponse> findRecycleItemsForAdmin(@Param("keyword") String keyword);

    // 관리자용 > 검색 결과 전체 개수
    int countRecycleItemsForAdmin(@Param("keyword") String keyword);

    // 관리자용 > 검색 + 페이징
    List<RecycleSearchResponse> findRecycleItemsForAdminPage(@Param("keyword") String keyword,
                                                             @Param("paging")Paging paging);

    // 관리자용 > 재활용 품목 수정
    int updateRecycleItem(RecycleItem recycleItem);
}
