package first_project.recycle.repository;

import first_project.recycle.domain.Reward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RewardMapper {
    List<Reward> findAll();
}
