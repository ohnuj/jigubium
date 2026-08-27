package first_project.recycle.repository;

import first_project.recycle.dto.AdminDashBoardResponse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    AdminDashBoardResponse findDashBoardData();


}
