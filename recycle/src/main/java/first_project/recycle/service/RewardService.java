package first_project.recycle.service;

import first_project.recycle.domain.EcoPointHistory;
import first_project.recycle.domain.Reward;
import first_project.recycle.domain.RewardExchange;
import first_project.recycle.repository.EcoPointHistoryMapper;
import first_project.recycle.repository.RewardExchangeMapper;
import first_project.recycle.repository.RewardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardMapper rewardMapper;
    private final RewardExchangeMapper rewardExchangeMapper;
    private final EcoPointHistoryMapper ecoPointHistoryMapper;

    //상점에 리워드 목록 띄우기
    public List<Reward> findAll(){
        return rewardMapper.findAll();
    }

    @Transactional
    public void exchangeReward(Long memberId, Long rewardId){
        //1. 교환하려는 리워드 조회
        Reward reward = rewardMapper.findById(rewardId);
        if(reward == null){
            throw new IllegalStateException("존재하지 않는 상품입니다");
        }
        //2. 재고확인
        if(reward.getStockQuantity() <= 0){
            throw new IllegalStateException("재고가 없습니다");
        }
        //3. 현재 에코포인트 조회
        int currentPoint = ecoPointHistoryMapper.findCurrentBalance(memberId);

        int requiredPoint = reward.getRequiredPoint();
        //4. 포인트 충분?
        if(currentPoint < requiredPoint){
            throw new IllegalStateException("에코포인트가 부족합니다");
        }
        //5. 재고 -1
        int stockResult = rewardMapper.minusStock(rewardId);
        if(stockResult == 0){
            throw new IllegalStateException("리워드 재고가 없습니다");
        }
        //6. 리워드 교환 내역
        RewardExchange rewardExchange = new RewardExchange();
        rewardExchange.setMemberId(memberId);
        rewardExchange.setRewardId(rewardId);
        rewardExchange.setPointUsed(requiredPoint);

        rewardExchangeMapper.insertExchange(rewardExchange);
        //7. 남은 포인트
        int balanceAfter = currentPoint - requiredPoint;
        //8. 에코포인트 사용 내역
        EcoPointHistory ecoPointHistory = new EcoPointHistory();

        ecoPointHistory.setMemberId(memberId);
        ecoPointHistory.setPointAmount(requiredPoint);
        ecoPointHistory.setBalanceAfter(balanceAfter);
        ecoPointHistory.setPointType("USE");
        ecoPointHistory.setReferenceType("REWARD");
        ecoPointHistory.setReferenceId(rewardExchange.getExchangeId());

        //9. 에코포인트 내역 저장
        ecoPointHistoryMapper.insertPointHistory(ecoPointHistory);


    }


}
