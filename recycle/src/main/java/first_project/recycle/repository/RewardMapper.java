package first_project.recycle.repository;

import first_project.recycle.domain.Reward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RewardMapper {
    List<Reward> findAll();
    Reward findById(@Param("rewardId") Long rewardId);
    int minusStock(@Param("rewardId") Long rewardId);
}
