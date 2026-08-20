package first_project.recycle.mapper;

import first_project.recycle.domain.ecoLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EcoLocationMapper {

    void insertEcoLocation(ecoLocation location);

    //중복 확인
    int countByRoadAddressAndLocationType(@Param("roadAddress")String roadAddress, @Param("locationType")String locationType);
}
