package first_project.recycle.repository;

import first_project.recycle.dto.RecycleSearchResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecycleMapper {
    List<RecycleSearchResponse> searchRecycleItems(@Param("keyword") String keyword);

    List<String> findExistingItemNames(@Param("itemNames") List<String> itemNames);
}
