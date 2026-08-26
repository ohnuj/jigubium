package first_project.recycle.controller;

import first_project.recycle.dto.RecycleSearchResponse;
import first_project.recycle.service.RecycleApiService;
import first_project.recycle.service.RecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recycle")
public class RecycleController {
    private final RecycleService recycleService;


    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model){
        List<RecycleSearchResponse> recycleItems = recycleService.searchRecycleItem(keyword);
        //검색어
        model.addAttribute("keyword",keyword);
        //검색 결과
        model.addAttribute("recycleItems",recycleItems);
        return "recycle/search";
    }
}
