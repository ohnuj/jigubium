package first_project.recycle.service;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.Paging;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

// 외부 API와 관계없이 DB만 조회
@Service
public class EcoLocationService {
    private final EcoLocationMapper ecoLocationMapper;

    public EcoLocationService(EcoLocationMapper ecoLocationMapper){
        this.ecoLocationMapper = ecoLocationMapper;
    }

    //타입별로 DB에서 조회하기
    public List<EcoLocation> getLocationsByType(String locationType){
        return ecoLocationMapper.findByLocationType(locationType);
    }

    // 검색 / 필터 결과 개수
    public int countEcoLocationForAdmin(String keyword, String locationType){
        return ecoLocationMapper.countEcoLocationsForAdmin(keyword, locationType);
    }
    // 관리자 > 수거함 전체 조회 + 주소검색 + 종류 필터
    public List<EcoLocation>  findEcoLocationsForAdmin(
            String keyword, String locationType, Paging paging){
        return ecoLocationMapper.findEcoLocationsForAdmin(keyword, locationType, paging);
    }

    // 관리자 > 수거함 종류 목록
    public List<String> findLocationTypes(){
        return ecoLocationMapper.findLocationTypes();
    }

    // 관리자 > ecoLocation 수정
    public void updateEcoLocation(EcoLocation ecoLocation){
        ecoLocationMapper.updateEcoLocation(ecoLocation);
    }

    //관리자 > eco Location 삭제
    public void deleteEcoLocation(Long itemId){
        ecoLocationMapper.deleteEcoLocation(itemId);
    }
}
