package first_project.recycle.controller;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.Paging;
import first_project.recycle.service.EcoLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
public class AdminEcoLocationController {
    private final EcoLocationService ecoLocationService;

    @GetMapping
    public String locationsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String locationType,
            Model model){

        int totalCount = ecoLocationService.countEcoLocationForAdmin(keyword, locationType);

        Paging paging = new Paging(page, 20, totalCount);

        model.addAttribute("locations",
                ecoLocationService.findEcoLocationsForAdmin(keyword, locationType, paging));

        model.addAttribute("locationTypes",ecoLocationService.findLocationTypes());

        model.addAttribute("paging",paging);

        int blockSize = 5;
        int startPage = ((paging.getPage() - 1) / blockSize) * blockSize + 1;

        int endPage = Math.min(startPage + blockSize - 1,paging.getTotalPages());

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        model.addAttribute("keyword", keyword);
        model.addAttribute("locationType", locationType);
        return "admin/ecoLocationList";
    }

    @PostMapping("/{locationId}/update")
    public String updateEcoLocation(@PathVariable Long locationId,
                                    EcoLocation ecoLocation,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "") String keyword,
                                    @RequestParam(defaultValue = "") String locationType,
                                    RedirectAttributes redirectAttributes){
        ecoLocation.setLocationId(locationId);

        ecoLocationService.updateEcoLocation(ecoLocation);

        redirectAttributes.addAttribute("page",page);
        redirectAttributes.addAttribute("keyword",keyword);
        redirectAttributes.addAttribute("locationType",locationType);

        return "redirect:/admin/locations";
    }

    @PostMapping("/{locationId}/delete")
    public String deleteEcoLocation(@PathVariable Long locationId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "") String keyword,
                                    @RequestParam(defaultValue = "") String locationType,
                                    RedirectAttributes redirectAttributes){
        ecoLocationService.deleteEcoLocation(locationId);

        redirectAttributes.addAttribute("page",page);
        redirectAttributes.addAttribute("keyword",keyword);
        redirectAttributes.addAttribute("locationType",locationType);

        return "redirect:/admin/locations";
    }
}
