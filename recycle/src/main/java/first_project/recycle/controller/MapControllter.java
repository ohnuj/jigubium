package first_project.recycle.controller;

import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationDTO.JongnoBatteryBinResponse;
import first_project.recycle.domain.ecoLocationDTO.JongnoClothingBinResponse;
import first_project.recycle.service.JongnoBatteryBinService;
import first_project.recycle.service.JongnoClothingBinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class MapControllter {

    //의류수거함 서비스 객체
    private final JongnoClothingBinService jongnoClothingBinService;

    //폐건전지 수거함 서비스 객체
    private final JongnoBatteryBinService jongnoBatteryBinService;

    //생성자 주입
    public MapControllter(
            JongnoClothingBinService jongnoClothingBinService,
            JongnoBatteryBinService jongnoBatteryBinService
    ) {
        this.jongnoClothingBinService = jongnoClothingBinService;
        this.jongnoBatteryBinService = jongnoBatteryBinService;
    }

    //지도 페이지 이동
    @GetMapping("/eco-map")
    public String map() {
        return "forward:/eco-map/recyclemap.html";
    }

    // api 출력을 도메인 구조에 맞게 변환
    // 종로구 의류수거함
    @GetMapping("/api/clothing-bins")
    @ResponseBody
    public List<ecoLocation> getClothingBins() {
        return jongnoClothingBinService.getClothingBins();
    }
    // 종로구 폐건전지 수거함 데이터
    // 현재는 API 응답 DTO를 그대로 반환해서 테스트
    @GetMapping("/api/battery-bins")
    @ResponseBody
    public List<ecoLocation> getBatteryBins() {
        return jongnoBatteryBinService.getBatteryBins();
    }



}
