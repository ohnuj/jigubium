package first_project.recycle.controller;

import first_project.recycle.domain.Reward;
import first_project.recycle.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;

    @GetMapping("/reward")
    public String reward(Model model){
        List<Reward> rewards = rewardService.findAll();
        model.addAttribute("rewards",rewards);
        return "reward/shop";
    }
}
