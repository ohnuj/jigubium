package first_project.recycle.service;

import first_project.recycle.domain.EcoPointHistory;
import first_project.recycle.repository.EcoPointHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoPointHistoryService {
    private final EcoPointHistoryMapper ecoPointHistoryMapper;


    public int insertPointHistory(EcoPointHistory ecoPointHistory) {
        return ecoPointHistoryMapper.insertPointHistory(ecoPointHistory);
    }

    // 에코포인트 획득 (회원가입, 게시글, 댓글, 게임)
    public void earnPoint(Long memberId,int point, String referenceType, Long referenceId){

        int exists = ecoPointHistoryMapper.existEarnPoint(memberId,referenceType,referenceId);
        if(exists>0){
            return;
        }

        int currentPoint = findCurrentBalance(memberId);

        EcoPointHistory history = new EcoPointHistory();

        history.setMemberId(memberId);
        history.setPointAmount(point);
        history.setBalanceAfter(currentPoint + point);
        history.setPointType("EARN");
        history.setReferenceType(referenceType);
        history.setReferenceId(referenceId);

        insertPointHistory(history);

    }

    // 에코포인트 환불 (reward 교환 거절)
    public void refundPoint(Long memberId,int point, String referenceType, Long referenceId){
        int currentPoint = findCurrentBalance(memberId);

        EcoPointHistory history = new EcoPointHistory();

        history.setMemberId(memberId);
        history.setPointAmount(point);
        history.setBalanceAfter(currentPoint + point);
        history.setPointType("REFUND");
        history.setReferenceType(referenceType);
        history.setReferenceId(referenceId);

        int result = insertPointHistory(history);

        if (result == 0){
            throw new IllegalStateException("에코포인트 환불 내역 저장에 실패했습니다.");
        }
    }

    //마이페이지용
    public int findCurrentBalance(Long memberId) {
        return ecoPointHistoryMapper.findCurrentBalance(memberId);
    }
    //뱃지용
    public int findTotalPoint(Long memberId) {
        return ecoPointHistoryMapper.findTotalPoint(memberId);
    }

    //포인트 사용 내역
    public List<EcoPointHistory> findByMemberId(Long memberId) {
        return ecoPointHistoryMapper.findByMemberId(memberId);
    }
}
