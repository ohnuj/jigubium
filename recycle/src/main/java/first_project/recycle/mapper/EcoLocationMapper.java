package first_project.recycle.mapper;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.Paging;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EcoLocationMapper {

    void insertEcoLocation(EcoLocation location);

    //중복 확인
    int countByRoadAddressAndLocationType(@Param("roadAddress")String roadAddress, @Param("locationType")String locationType);

    // 장소 타입별로 DB에서 조회
    List<EcoLocation> findByLocationType(
            @Param("locationType") String locationType
    );

    // 관리자 > 수거함 전체 조회 + 주소검색 + 종류 필터
    List<EcoLocation> findEcoLocationsForAdmin(
            @Param("keyword") String keyword,
            @Param("locationType") String locationType,
            @Param("paging") Paging paging
    );

    // 관리자 > 페이징을 위한 총 개수 count
    int countEcoLocationsForAdmin(
            @Param("keyword") String keyword,
            @Param("locationType")  String locationType
    );

    // 관리자 > 수거함 종류 목록
    List<String> findLocationTypes();

    int updateEcoLocation(EcoLocation ecoLocation);

    int deleteEcoLocation(@Param("locationId") Long locationId);
}
