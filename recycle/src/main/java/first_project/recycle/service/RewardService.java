package first_project.recycle.service;

import first_project.recycle.domain.EcoPointHistory;
import first_project.recycle.domain.Reward;
import first_project.recycle.domain.RewardExchange;
import first_project.recycle.domain.RewardRequest;
import first_project.recycle.exception.NotFoundException;
import first_project.recycle.repository.RewardExchangeMapper;
import first_project.recycle.repository.RewardMapper;
import first_project.recycle.repository.RewardRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardMapper rewardMapper;
    private final RewardExchangeMapper rewardExchangeMapper;
    private final EcoPointHistoryService ecoPointHistoryService;
    private final RewardRequestMapper rewardRequestMapper;

    //상점에 리워드 목록 띄우기
    public List<Reward> findAll(){
        return rewardMapper.findAll();
    }

    // 리워드 단건 조회
    public Reward findById(Long rewardId){
        Reward reward = rewardMapper.findById(rewardId);
        if (reward == null){
            throw new NotFoundException("존재하지 않은 상품입니다");
        }
        return reward;
    }


    // 회원별 리워드 교환 내역
    public List<RewardExchange> findByMemberId(Long memberId){
        return rewardExchangeMapper.findByMemberId(memberId);
    }

    // 관리자 > 전체 리워드 교환 요청 조회
    public List<RewardExchange> findAllExchange(){
        return rewardExchangeMapper.findAll();
    }

    // 리워드 교환 요청
    @Transactional
    public void exchangeReward(Long memberId, Long rewardId, RewardRequest rewardRequest){
        //1. 교환하려는 리워드 조회
        Reward reward = rewardMapper.findById(rewardId);
        if(reward == null){
            throw new NotFoundException("존재하지 않는 상품입니다");
        }
        //2. 재고확인
        if(reward.getStockQuantity() <= 0){
            throw new IllegalStateException("재고가 없습니다");
        }
        // 교환 요청 정보 확인
        if (rewardRequest == null){
            throw new IllegalArgumentException("교환 요청 정보가 없습니다.");
        }
        if (rewardRequest.getName() == null) {
            throw new IllegalArgumentException("이름을 입력해주세요");
        }
        if (rewardRequest.getPhone() == null) {
            throw new IllegalArgumentException("전화번호를 입력해주세요");
        }
        if (rewardRequest.getAddress() == null) {
            throw new IllegalArgumentException("주소를 입력해주세요");
        }
        //3. 현재 에코포인트 조회
        int currentPoint = ecoPointHistoryService.findCurrentBalance(memberId);

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
        //6. 리워드 교환 요청 저장
        RewardExchange rewardExchange = new RewardExchange();

        rewardExchange.setMemberId(memberId);
        rewardExchange.setRewardId(rewardId);
        rewardExchange.setPointUsed(requiredPoint);

        int exchangeResult = rewardExchangeMapper.insertExchange(rewardExchange);

        if (exchangeResult == 0 || rewardExchange.getExchangeId() == null){
            throw new IllegalStateException("리워드 교환 요청 저장에 실패했습니다.");
        }

        //7. 교환 신청 정보에 exchangeId 연결
        rewardRequest.setExchangeId(rewardExchange.getExchangeId());

        //8. 이름, 번호, 주소 저장
        int requestResult = rewardRequestMapper.insertRequest(rewardRequest);

        if (requestResult == 0){
            throw new IllegalStateException("리워드 신청 정보 저장에 실패했습니다.");
        }

        //9. 남은 포인트
        int balanceAfter = currentPoint - requiredPoint;

        //8. 에코포인트 사용 내역
        EcoPointHistory ecoPointHistory = new EcoPointHistory();

        ecoPointHistory.setMemberId(memberId);
        ecoPointHistory.setPointAmount(requiredPoint);
        ecoPointHistory.setBalanceAfter(balanceAfter);
        ecoPointHistory.setPointType("USE");
        ecoPointHistory.setReferenceType("REWARD");
        ecoPointHistory.setReferenceId(rewardExchange.getExchangeId());

        //10. 에코포인트 내역 저장
        int pointResult = ecoPointHistoryService.insertPointHistory(ecoPointHistory);

        if(pointResult == 0){
            throw new IllegalStateException("에코포인트 사용 내역 저장에 실패했습니다.");
        }
    }

    // 관리자 > 교환 요청 완료
    @Transactional
    public void completeExchange(Long exchangeId){

        RewardExchange exchange = rewardExchangeMapper.findById(exchangeId);

        if (exchange == null){
            throw new NotFoundException("존재하지 않는 리워드 교환 요청입니다.");
        }

        int result = rewardExchangeMapper.completeExchange(exchangeId);

        if (result == 0){
            throw new IllegalStateException("이미 처리된 리워드 교환 요청입니다.");
        }
    }

    // 관리자 > 교환 요청 거절
    @Transactional
    public void rejectExchange(Long exchangeId){

        RewardExchange exchange = rewardExchangeMapper.findById(exchangeId);

        if (exchange == null){
            throw new NotFoundException("존재하지 않는 리워드 교환 요청입니다.");
        }

        int result = rewardExchangeMapper.rejectExchange(exchangeId);

        if (result == 0){
            throw new IllegalStateException("이미 처리된 리워드 교환 요청입니다.");
        }

        // 재고 복구
        int stockResult = rewardMapper.plusStock(exchange.getRewardId());

        if(stockResult == 0){
            throw new IllegalStateException("리워드 재고 복구에 실패했습니다.");
        }

        ecoPointHistoryService.refundPoint(
                exchange.getMemberId(),
                exchange.getPointUsed(),
                "REWARD",
                exchange.getExchangeId()
        );
    }


}
