package first_project.recycle.controller;

import first_project.recycle.domain.Paging;
import first_project.recycle.domain.RecycleItem;
import first_project.recycle.service.RecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/recycle-items")
@RequiredArgsConstructor
public class AdminRecycleController {

    private final RecycleService recycleService;

    //관리자 > 재활용 품목 전체 목록
    @GetMapping
    public String recycleItemList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            Model model) {

        // 검색시 전체 품목 개수
        int totalCount = recycleService.countRecycleItemsForAdmin(keyword);

        // 10개 / 1 page
        Paging paging = new Paging(page, 10,totalCount);

        // 검색+페이징 목록
        model.addAttribute("items", recycleService.findRecycleItemsForAdminPage(keyword, paging));
        //페이징 정보
        model.addAttribute("paging", paging);

        int blockSize = 5;
        int startPage = ((paging.getPage() - 1) / blockSize) * blockSize + 1;

        int endPage = Math.min(startPage + blockSize - 1,paging.getTotalPages());

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        //검색어
        model.addAttribute("keyword", keyword);
        return "admin/recycleList";
    }

    @PostMapping("/{itemId}/update")
    public String updateRecycleItem(@PathVariable Long itemId,
                                    RecycleItem recycleItem,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "") String keyword,
                                    RedirectAttributes redirectAttributes){

        // URL > itemId
        recycleItem.setItemId(itemId);

        //DB update
        recycleService.updateRecycleItem(recycleItem);

        redirectAttributes.addAttribute("page",page);
        redirectAttributes.addAttribute("keyword",keyword);

        return "redirect:/admin/recycle-items";
    }
    @PostMapping("/{itemId}/delete")
    public String deleteRecycleItem(@PathVariable Long itemId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "") String keyword,
                                    RedirectAttributes redirectAttributes){


        //DB delete
        recycleService.deleteRecycleItem(itemId);

        redirectAttributes.addAttribute("page",page);
        redirectAttributes.addAttribute("keyword",keyword);

        return "redirect:/admin/recycle-items";
    }
}
