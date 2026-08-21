package first_project.recycle.controller;

import first_project.recycle.dto.RecycleApiResponse;
import first_project.recycle.dto.RecycleSearchResponse;
import first_project.recycle.service.RecycleApiService;
import first_project.recycle.service.RecycleService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recycle")
public class RecycleController {
    private final RecycleService recycleService;
    private final RecycleApiService recycleApiService;


    @GetMapping("/search")
    public String search(@Param("keyword") String keyword, Model model){
        List<RecycleSearchResponse> recycleItems = recycleService.searchRecycleItem(keyword);
        //검색어
        model.addAttribute("keyword",keyword);
        //검색 결과
        model.addAttribute("recycleItems",recycleItems);
        return "recycle/search";
    }

    @ResponseBody
    @GetMapping("/api-test")
    public List<RecycleApiResponse.Item> apiTest(@RequestParam("keyword") String keyword){
        return recycleApiService.searchRecycleApi(keyword);
    }

}
