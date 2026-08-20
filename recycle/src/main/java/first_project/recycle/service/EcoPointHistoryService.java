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
