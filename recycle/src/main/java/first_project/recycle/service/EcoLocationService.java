package first_project.recycle.service;

import first_project.recycle.domain.ecoLocation;
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
    public List<ecoLocation> getLocationsByType(String locationType){
        return ecoLocationMapper.findByLocationType(locationType);
    }
}
