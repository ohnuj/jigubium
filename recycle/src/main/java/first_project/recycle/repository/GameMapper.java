package first_project.recycle.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface GameMapper {
    int countTodayGamesByMemberId(@Param("memberId") Long memberId);

    void insertGameHistory(Map<String, Object> paramMap);

    List<Map<String, String>> findRandomItems(@Param("count") int count);
}