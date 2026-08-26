package first_project.recycle.repository;

import first_project.recycle.domain.Badge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BadgeMapper {
    Badge findCurrentBadge(@Param("totalPoint") int totalPoint);
}
