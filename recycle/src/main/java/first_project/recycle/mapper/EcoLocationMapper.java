package first_project.recycle.mapper;

import first_project.recycle.domain.ecoLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EcoLocationMapper {

    void insertEcoLocation(ecoLocation location);

    //중복 확인
    int countByRoadAddressAndLocationType(@Param("roadAddress")String roadAddress, @Param("locationType")String locationType);

    // 장소 타입별로 DB에서 조회
    List<ecoLocation> findByLocationType(
            @Param("locationType") String locationType
    );

}
