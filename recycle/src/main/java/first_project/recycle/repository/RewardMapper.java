package first_project.recycle.repository;

import first_project.recycle.domain.Reward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RewardMapper {

    // 전체 리워드 조회
    List<Reward> findAll();

    Reward findById(@Param("rewardId") Long rewardId);

    // 교환시 재고 차감
    int minusStock(@Param("rewardId") Long rewardId);

    // 교환 거절시 재고 다시 추가
    int plusStock(@Param("rewardId") Long rewardId);
}
