package first_project.recycle.service;

import first_project.recycle.domain.Paging;
import first_project.recycle.domain.RecycleItem;
import first_project.recycle.dto.RecycleApiResponse;
import first_project.recycle.dto.RecycleSearchResponse;
import first_project.recycle.exception.NotFoundException;
import first_project.recycle.repository.RecycleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecycleService {
    private final RecycleMapper recycleMapper;
    private final RecycleApiService recycleApiService;

    public List<RecycleSearchResponse> searchRecycleItem(String keyword){

        //DB검색
        List<RecycleSearchResponse> recycleItems = recycleMapper.searchRecycleItems(keyword);

        //API검색
        List<RecycleApiResponse.Item> apiItems = recycleApiService.searchRecycleApi(keyword);

        //DB와 API itemNm같으면 합치기
        for(RecycleSearchResponse recycleItem : recycleItems){
            for(RecycleApiResponse.Item apiItem : apiItems){
                if (recycleItem.getItemName().equals(apiItem.itemNm())){
                    recycleItem.setDischargeMethod(apiItem.dschgMthd());
                    break;
                }
            }

            //관련품목 처리
            if (recycleItem.getSearchKeyword() != null &&
                    !recycleItem.getSearchKeyword().isBlank()){
                List<String> relatedItemNames =
                        Arrays.stream(recycleItem.getSearchKeyword().split(","))
                                .map(String::trim).filter(name -> !name.isBlank()).toList();

                //실제 검색 가능한 관련품목만 보이게
                List<String> existingItems = recycleMapper.findExistingItemNames(relatedItemNames);
                recycleItem.setRelatedItems(existingItems);
            }
        }

        return recycleItems;
    }

    // 관리자 > 재활용 품목 전체 조회
    public List<RecycleSearchResponse> findRecycleItemsForAdmin(String keyword){
        return recycleMapper.findRecycleItemsForAdmin(keyword);
    }

    // 관리자 > 검색 결과 전체 개수
    public int countRecycleItemsForAdmin(String keyword){
        return recycleMapper.countRecycleItemsForAdmin(keyword);
    }

    // 관리자 > 검색+페이지 목록
    public List<RecycleSearchResponse> findRecycleItemsForAdminPage(String keyword, Paging paging){
        return recycleMapper.findRecycleItemsForAdminPage(keyword,paging);
    }

    // 관리자 > 재활용 품목 수정
    public void updateRecycleItem(RecycleItem recycleItem){
        int result = recycleMapper.updateRecycleItem(recycleItem);
        if (result == 0){
            throw new NotFoundException("존재하지 않는 재활용 품목입니다.");
        }
    }

    // 관리자 > 재활용 품목 삭제
    public void deleteRecycleItem(Long itemId){
        int result = recycleMapper.deleteRecycleItem(itemId);
        if (result == 0){
            throw new NotFoundException("존재하지 않는 재활용 품목입니다.");
        }
    }
}
