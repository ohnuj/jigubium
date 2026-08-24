package first_project.recycle.controller;

import first_project.recycle.domain.ecoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.domain.ecoLocationdto.seodaemun.SeodaemunMedicineBinResponse;
import first_project.recycle.service.mapservice.dongjakservice.DongjakBatteryBinService;
import first_project.recycle.service.mapservice.dongjakservice.DongjakClothingBinService;
import first_project.recycle.service.mapservice.dongjakservice.DongjakMedicineBinService;
import first_project.recycle.service.mapservice.seodaemunservice.SeodaemunClothingBinService;
import first_project.recycle.service.mapservice.wasteelectronicsservice.WasteElectronicsService;
import first_project.recycle.service.EcoLocationService;
import first_project.recycle.service.mapservice.jongnoservice.JongnoBatteryBinService;
import first_project.recycle.service.mapservice.jongnoservice.JongnoClothingBinService;
import first_project.recycle.service.mapservice.yongsanservice.YongsanClothingBinService;
import first_project.recycle.service.mapservice.yongsanservice.YongsanMedicineBinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import first_project.recycle.service.mapservice.gwanakservice.GwanakClothingBinService;
import first_project.recycle.service.mapservice.gwanakservice.GwanakBatteryBinService;
import first_project.recycle.service.mapservice.gwanakservice.GwanakMedicineBinService;
import first_project.recycle.service.mapservice.seodaemunservice.SeodaemunMedicineBinService;
import first_project.recycle.domain.ecoLocationdto.seodaemun.SeodaemunMedicineBinResponse;
import java.util.List;

@Controller
public class MapControllter {

    //종로 의류수거함 서비스 객체
    private final JongnoClothingBinService jongnoClothingBinService;

    //종로폐건전지 수거함 서비스 객체
    private final JongnoBatteryBinService jongnoBatteryBinService;
    //용산 의류수거함 서비스객체
    private final YongsanClothingBinService yongsanClothingBinService;
    // 용산 폐의약품 서비스 객체
    private final YongsanMedicineBinService yongsanMedicineBinService;
    // 관악구 의류수거함 서비스 객체
    private final GwanakClothingBinService gwanakClothingBinService;
    // 관악구 폐건전지·폐형광등 서비스 객체
    private final GwanakBatteryBinService gwanakBatteryBinService;
    // 관악구 폐의약품 수거함 서비스 객체
    private final GwanakMedicineBinService gwanakMedicineBinService;
    // 폐가전 API 서비스 객체
    private final WasteElectronicsService wasteElectronicsService;
    // 서대문구 폐의약품 수거함 서비스 객체
    private final SeodaemunMedicineBinService seodaemunMedicineBinService;
    // 서대문구 의류수거함 서비스 객체
    private final SeodaemunClothingBinService seodaemunClothingBinService;
    // 동작구 폐건전지·폐형광등 서비스 객체
    private final DongjakBatteryBinService dongjakBatteryBinService;
    // 동작구 의류수거함 서비스 객체
    private final DongjakClothingBinService dongjakClothingBinService;
    // 동작구 폐의약품 수거함 서비스 객체
    private final DongjakMedicineBinService dongjakMedicineBinService;

    //DB 조회용 서비스 객체
    private final EcoLocationService ecoLocationService;


    //생성자 주입
    public MapControllter(
            JongnoClothingBinService jongnoClothingBinService,
            JongnoBatteryBinService jongnoBatteryBinService,
            EcoLocationService ecoLocationService,
            YongsanClothingBinService yongsanClothingBinService,
            YongsanMedicineBinService yongsanMedicineBinService,
            GwanakClothingBinService gwanakClothingBinService,
            GwanakBatteryBinService gwanakBatteryBinService,
            GwanakMedicineBinService gwanakMedicineBinService,
            WasteElectronicsService wasteElectronicsService,
            SeodaemunMedicineBinService seodaemunMedicineBinService,
            SeodaemunClothingBinService seodaemunClothingBinService,
            DongjakBatteryBinService dongjakBatteryBinService,
            DongjakClothingBinService dongjakClothingBinService,
            DongjakMedicineBinService dongjakMedicineBinService
    ) {
        this.jongnoClothingBinService = jongnoClothingBinService;
        this.jongnoBatteryBinService = jongnoBatteryBinService;
        this.ecoLocationService = ecoLocationService;
        this.yongsanClothingBinService = yongsanClothingBinService;
        this.yongsanMedicineBinService = yongsanMedicineBinService;
        this.gwanakClothingBinService = gwanakClothingBinService;
        this.gwanakBatteryBinService = gwanakBatteryBinService;
        this.gwanakMedicineBinService = gwanakMedicineBinService;
        this.wasteElectronicsService = wasteElectronicsService;
        this.seodaemunMedicineBinService = seodaemunMedicineBinService;
        this.seodaemunClothingBinService = seodaemunClothingBinService;
        this.dongjakBatteryBinService = dongjakBatteryBinService;
        this.dongjakClothingBinService = dongjakClothingBinService;
        this.dongjakMedicineBinService = dongjakMedicineBinService;
    }

    //지도 페이지 이동
    @GetMapping("/eco-map")
    public String map() {
        return "eco-map/recyclemap";
    }

    // api 출력을 도메인 구조에 맞게 변환
    // 종로구 의류수거함
    @GetMapping("/api/clothing-bins")
    @ResponseBody
    public List<ecoLocation> getClothingBins() {
        return ecoLocationService.getLocationsByType("의류수거함");
    }

    // 종로구 폐건전지 수거함 데이터
    // 현재는 API 응답 DTO를 그대로 반환해서 테스트
    @GetMapping("/api/battery-bins")
    @ResponseBody
    public List<ecoLocation> getBatteryBins() {
        return ecoLocationService.getLocationsByType("폐건전지·폐형광등 수거함");
    }

    // 용산구 의류수거함 API 호출 및 DB 저장 테스트
    @GetMapping("/api/yongsan-clothing-bins/import")
    @ResponseBody
    public List<ecoLocation> importYongsanClothingBins() {
        return yongsanClothingBinService.getClothingBins();
    }

    // 용산구 폐의약품 API 호출 및 DB 저장
    @GetMapping("/api/yongsan-medicine-bins/import")
    @ResponseBody
    public List<ecoLocation> importYongsanMedicineBins() {
        return yongsanMedicineBinService.getMedicineBins();
    }
    // 폐의약품 수거함 DB 조회
    @GetMapping("/api/medicine-bins")
    @ResponseBody
    public List<ecoLocation> getMedicineBins() {
        return ecoLocationService.getLocationsByType("폐의약품 수거함");
    }
    //관악구 csv db에 저장
    @GetMapping("/api/gwanak-clothing-bins/import")
    @ResponseBody
    public List<ecoLocation> importGwanakClothingBins() {
        return gwanakClothingBinService.importClothingBins();
    }
    // 관악구 폐건전지·폐형광등 CSV DB 저장
    @GetMapping("/api/gwanak-battery-bins/import")
    @ResponseBody
    public List<ecoLocation> importGwanakBatteryBins() {
        return gwanakBatteryBinService.importBatteryBins();
    }
    // 관악구 폐의약품 CSV DB 저장
    @GetMapping("/api/gwanak-medicine-bins/import")
    @ResponseBody
    public List<ecoLocation> importGwanakMedicineBins() {
        return gwanakMedicineBinService.importMedicineBins();
    }
    // 서대문구 폐의약품 API 호출 및 DB 저장 테스트
    @GetMapping("/api/seodaemun-medicine-bins/import")
    @ResponseBody
    public List<ecoLocation> importSeodaemunMedicineBins() {
        return seodaemunMedicineBinService.getMedicineBins();
    }
    // 서대문구 의류수거함 API 호출 및 DB 저장
    @GetMapping("/api/seodaemun-clothing-bins/import")
    @ResponseBody
    public List<ecoLocation> importSeodaemunClothingBins() {
        return seodaemunClothingBinService.getClothingBins();
    }
    //동작구 폐건전지, 폐형광등 api 저장
    @GetMapping("/api/dongjak-battery-bins/import")
    @ResponseBody
    public List<ecoLocation> importDongjakBatteryBins() {
        return dongjakBatteryBinService.getBatteryBins();
    }
    // 동작구 의류수거함 API 호출 및 DB 저장
    @GetMapping("/api/dongjak-clothing-bins/import")
    @ResponseBody
    public List<ecoLocation> importDongjakClothingBins() {
        return dongjakClothingBinService.getClothingBins();}
    //동작구 폐의약품 api 호출 및 DB 저장
    @GetMapping("/api/dongjak-medicine-bins/import")
    @ResponseBody
    public List<ecoLocation> importDongjakMedicineBins(){
        return dongjakMedicineBinService.getMedicineBins();}
    // 폐가전 CSV DB 저장
    @GetMapping("/api/waste-electronics/import")
    @ResponseBody
    public List<ecoLocation> importWasteElectronics() {
        return wasteElectronicsService.importWasteElectronics();
    }
    // 폐가전 수거함 DB 조회
    @GetMapping("/api/waste-electronics")
    @ResponseBody
    public List<ecoLocation> getWasteElectronics() {
        return ecoLocationService.getLocationsByType(
                "폐가전 수거함");}



}


/**
 * 공공데이터 API
 * → ecoLocation 변환
 * → DB 저장
 *
 * 지도 페이지
 * → /api/clothing-bins 또는 /api/battery-bins
 * → EcoLocationService
 * → MyBatis Mapper
 * → eco_location DB 조회
 * → 지도 마커 표시
 */