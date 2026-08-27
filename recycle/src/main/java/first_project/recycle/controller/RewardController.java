package first_project.recycle.controller;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.Reward;
import first_project.recycle.domain.SessionUser;
import first_project.recycle.service.EcoPointHistoryService;
import first_project.recycle.service.RewardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;
    private final EcoPointHistoryService ecoPointHistoryService;

    @GetMapping("/reward")
    public String rewardShop(HttpServletRequest request, Model model){
        HttpSession session = request.getSession(false);
        //로그인 회원
        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginUser.getMemberId();

        //리워드 목록
        List<Reward> rewards = rewardService.findAll();
        //현재 에코포인트
        int currentPoint = ecoPointHistoryService.findCurrentBalance(memberId);
        model.addAttribute("rewards",rewards);
        model.addAttribute("currentPoint",currentPoint);
        return "reward/shop";
    }

    @PostMapping("/reward/exchange")
    public String exchange(@RequestParam("rewardId") Long rewardId,
                           HttpServletRequest request, RedirectAttributes redirectAttributes){
        HttpSession session = request.getSession(false);

        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginUser.getMemberId();
        try {
                rewardService.exchangeReward(memberId,rewardId);
                redirectAttributes.addFlashAttribute(
                        "message","리워드 교환이 완료되었습니다");

        }catch (IllegalStateException e){
            redirectAttributes.addFlashAttribute("eMessage",e.getMessage());
        }
        return "redirect:/reward";

    }
}
