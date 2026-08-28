package first_project.recycle.repository;

import first_project.recycle.domain.RewardRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RewardRequestMapper {
    int insertRequest(RewardRequest rewardRequest);
    RewardRequest findByExchangeId(@Param("exchangeId") Long exchangeId);
}
