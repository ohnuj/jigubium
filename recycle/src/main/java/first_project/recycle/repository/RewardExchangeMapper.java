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
}
