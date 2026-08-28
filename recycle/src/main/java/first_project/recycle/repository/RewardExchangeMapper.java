package first_project.recycle.repository;

import first_project.recycle.domain.RewardExchange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RewardExchangeMapper {
    //리워드 교환 기록 저장
    int insertExchange(RewardExchange rewardExchange);

    //리워드 교환 목록
    List<RewardExchange> findByMemberId(@Param("memberId") Long memberId);

    // 교환 요청 단건 조회
    RewardExchange findById(@Param("exchangeId") Long exchangeId);

    // 관리자 > 전체 교환 요청 조회
    List<RewardExchange> findAll();

    // 관리자 메인 - 리워드 요청 요약 5개
    List<RewardExchange> findAdminSummary();

    int countRequested();

    // 관리자 > 교환 요청 처리 완료
    int completeExchange(@Param("exchangeId") Long exchangeId);

    // 관리자 > 교환 요청 거절
    int rejectExchange(@Param("exchangeId") Long exchangeId);
}
