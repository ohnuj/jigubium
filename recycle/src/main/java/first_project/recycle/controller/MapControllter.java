package first_project.recycle.controller;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.service.mapservice.dobongservice.DobongBatteryBinService;
import first_project.recycle.service.mapservice.dongdaemunservice.DongdaemunBatteryBinService;
import first_project.recycle.service.mapservice.dongdaemunservice.DongdaemunClothingBinService;
import first_project.recycle.service.mapservice.dongdaemunservice.DongdaemunMedicineBinService;
import first_project.recycle.service.mapservice.dongjakservice.DongjakBatteryBinService;
import first_project.recycle.service.mapservice.dongjakservice.DongjakClothingBinService;
import first_project.recycle.service.mapservice.dongjakservice.DongjakMedicineBinService;
import first_project.recycle.service.mapservice.eunpyeongservice.EunpyeongBatteryBinService;
import first_project.recycle.service.mapservice.eunpyeongservice.EunpyeongClothingBinService;
import first_project.recycle.service.mapservice.gangbukservice.GangbukClothingBinService;
import first_project.recycle.service.mapservice.gangdongservice.GangdongClothingBinService;
import first_project.recycle.service.mapservice.gangdongservice.GangdongMedicineBinService;
import first_project.recycle.service.mapservice.gangnamservice.GangnamBatteryBinService;
import first_project.recycle.service.mapservice.gangnamservice.GangnamClothingBinService;
import first_project.recycle.service.mapservice.gangseoservice.GangseoBatteryBinService;
import first_project.recycle.service.mapservice.gangseoservice.GangseoClothingBinService;
import first_project.recycle.service.mapservice.geumcheon.GeumcheonClothingBinService;
import first_project.recycle.service.mapservice.guroservice.GuroClothingBinService;
import first_project.recycle.service.mapservice.gwangjinservice.GwangjinBatteryBinService;
import first_project.recycle.service.mapservice.gwangjinservice.GwangjinClothingBinService;
import first_project.recycle.service.mapservice.jungnangservice.JungnangBatteryBinService;
import first_project.recycle.service.mapservice.jungnangservice.JungnangClothingBinService;
import first_project.recycle.service.mapservice.maposervice.MapoBatteryBinService;
import first_project.recycle.service.mapservice.maposervice.MapoMedicineBinService;
import first_project.recycle.service.mapservice.nowonservice.NowonClothingBinService;
import first_project.recycle.service.mapservice.seochoservice.SeochoClothingBinService;
import first_project.recycle.service.mapservice.seodaemunservice.SeodaemunClothingBinService;
import first_project.recycle.service.mapservice.seongbukservice.SeongbukBatteryBinService;
import first_project.recycle.service.mapservice.seongbukservice.SeongbukClothingBinService;
import first_project.recycle.service.mapservice.seongdong.SeongdongBatteryBinService;
import first_project.recycle.service.mapservice.seongdong.SeongdongClothingBinService;
import first_project.recycle.service.mapservice.songpaservice.SongpaBatteryBinService;
import first_project.recycle.service.mapservice.songpaservice.SongpaClothingBinService;
import first_project.recycle.service.mapservice.songpaservice.SongpaMedicineBinService;
import first_project.recycle.service.mapservice.wasteelectronicsservice.WasteElectronicsService;
import first_project.recycle.service.EcoLocationService;
import first_project.recycle.service.mapservice.jongnoservice.JongnoBatteryBinService;
import first_project.recycle.service.mapservice.jongnoservice.JongnoClothingBinService;
import first_project.recycle.service.mapservice.yangcheonservice.YangcheonBatteryBinService;
import first_project.recycle.service.mapservice.yangcheonservice.YangcheonClothingBinService;
import first_project.recycle.service.mapservice.yangcheonservice.YangcheonMedicineBinService;
import first_project.recycle.service.mapservice.yeongdeungposervice.YeongdeungpoBatteryBinService;
import first_project.recycle.service.mapservice.yeongdeungposervice.YeongdeungpoClothingBinService;
import first_project.recycle.service.mapservice.yongsanservice.YongsanClothingBinService;
import first_project.recycle.service.mapservice.yongsanservice.YongsanMedicineBinService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import first_project.recycle.service.mapservice.gwanakservice.GwanakClothingBinService;
import first_project.recycle.service.mapservice.gwanakservice.GwanakBatteryBinService;
import first_project.recycle.service.mapservice.gwanakservice.GwanakMedicineBinService;
import first_project.recycle.service.mapservice.seodaemunservice.SeodaemunMedicineBinService;

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

    // 영등포구 의류수거함 서비스 객체
    private final YeongdeungpoClothingBinService yeongdeungpoClothingBinService;
    // 영등포구 폐건전지,폐형광등 서비스
    private final YeongdeungpoBatteryBinService yeongdeungpoBatteryBinService;

    // 강남구 의류수거함 서비스 객체
    private final GangnamClothingBinService gangnamClothingBinService;
    // 강남구 폐건전지, 폐형광등 서비스
    private final GangnamBatteryBinService gangnamBatteryBinService;

    // 성북구 의류수거함 서비스 객체
    private final SeongbukClothingBinService seongbukClothingBinService;
    // 성북구 폐건전지,폐형광등 서비스
    private final SeongbukBatteryBinService seongbukBatteryBinService;

    // 중랑구 의류수거함 서비스 객체
    private final JungnangClothingBinService jungnangClothingBinService;
    // 중랑구 폐건전지, 폐형광등 API 서비스 객체
    private final JungnangBatteryBinService jungnangBatteryBinService;

    // 양천구 의류수거함 api 서비스 객체
    private final YangcheonClothingBinService yangcheonClothingBinService;
    // 양천구 폐건전지, 폐형광등 API 서비스 객체
    private final YangcheonBatteryBinService yangcheonBatteryBinService;
    // 양천구 폐의약품 수거장소 서비스 객체
    private final YangcheonMedicineBinService yangcheonMedicineBinService;

    // 송파구 의류수거함 서비스 객체
    private final SongpaClothingBinService songpaClothingBinService;
    // 송파구 폐건전지, 폐형광등 서비스 객체
    private final SongpaBatteryBinService songpaBatteryBinService;
    // 송파구 폐의약품 수거장소 서비스 객체
    private final SongpaMedicineBinService songpaMedicineBinService;

    // 구로구 의류수거함 서비스 객체
    private final GuroClothingBinService guroClothingBinService;

    // 마포구 폐건전지,폐형광등 서비스 객체
    private final MapoBatteryBinService mapoBatteryBinService;
    // 마포구 폐의약품 수거장소 서비스 객체
    private final MapoMedicineBinService mapoMedicineBinService;

    // 성동구 의류수거함 서비스 객체
    private final SeongdongClothingBinService seongdongClothingBinService;
    // 성동구 폐건전지, 폐형광등 서비스 객체
    private final SeongdongBatteryBinService seongdongBatteryBinService;

    // 광진구 의류수거함 서비스 객체
    private final GwangjinClothingBinService gwangjinClothingBinService;
    // 광진구 폐건전지, 폐형광등 서비스 객체
    private final GwangjinBatteryBinService gwangjinBatteryBinService;

    // 동대문구 의류수거함 서비스 객체
    private final DongdaemunClothingBinService dongdaemunClothingBinService;
    // 동대문구 폐건전지, 폐형광등 서비스 객체
    private final DongdaemunBatteryBinService dongdaemunBatteryBinService;
    // 동대문구 폐의약품 수거 서비스 객체
    private final DongdaemunMedicineBinService dongdaemunMedicineBinService;

    // 강북구 의류수거함 서비스 객체
    private final GangbukClothingBinService gangbukClothingBinService;

    // 도봉구 폐건전지,폐형광등 서비스 객체
    private final DobongBatteryBinService dobongBatteryBinService;

    // 노원구 의류수거함 서비스 객체
    private final NowonClothingBinService nowonClothingBinService;

    // 은평구 의류수거함 서비스 객체
    private final EunpyeongClothingBinService eunpyeongClothingBinService;
    // 은평구 폐건전지,폐형광등 서비스 객체
    private final EunpyeongBatteryBinService eunpyeongBatteryBinService;

    // 강서구 의류수거함 서비스 객체
    private final GangseoClothingBinService gangseoClothingBinService;
    // 강서구 폐건전지,폐형광등 서비스 객체
    private final GangseoBatteryBinService gangseoBatteryBinService;

    // 금천구 의류수거함 서비스 객체
    private final GeumcheonClothingBinService geumcheonClothingBinService;

    // 서초구 의류수거함 서비스 객체
    private final SeochoClothingBinService seochoClothingBinService;

    // 강동구 의류수거함 서비스 객체
    private final GangdongClothingBinService gangdongClothingBinService;
    // 강동구 폐의약품 수거장소 서비스 객체
    private final GangdongMedicineBinService gangdongMedicineBinService;

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
            DongjakMedicineBinService dongjakMedicineBinService,
            YeongdeungpoClothingBinService yeongdeungpoClothingBinService,
            YeongdeungpoBatteryBinService yeongdeungpoBatteryBinService,
            GangnamClothingBinService gangnamClothingBinService,
            GangnamBatteryBinService gangnamBatteryBinService,
            SeongbukClothingBinService seongbukClothingBinService,
            SeongbukBatteryBinService seongbukBatteryBinService,
            JungnangBatteryBinService jungnangBatteryBinService,
            JungnangClothingBinService jungnangClothingBinService,
            YangcheonBatteryBinService yangcheonBatteryBinService,
            YangcheonClothingBinService yangcheonClothingBinService,
            YangcheonMedicineBinService yangcheonMedicineBinService,
            SongpaClothingBinService songpaClothingBinService,
            SongpaBatteryBinService songpaBatteryBinService,
            SongpaMedicineBinService songpaMedicineBinService,
            GuroClothingBinService guroClothingBinService,
            MapoBatteryBinService mapoBatteryBinService,
            MapoMedicineBinService mapoMedicineBinService,
            SeongdongClothingBinService seongdongClothingBinService,
            SeongdongBatteryBinService seongdongBatteryBinService,
            GwangjinClothingBinService gwangjinClothingBinService,
            GwangjinBatteryBinService gwangjinBatteryBinService,
            DongdaemunClothingBinService dongdaemunClothingBinService,
            DongdaemunBatteryBinService dongdaemunBatteryBinService,
            DongdaemunMedicineBinService dongdaemunMedicineBinService,
            GangbukClothingBinService gangbukClothingBinService,
            DobongBatteryBinService dobongBatteryBinService,
            NowonClothingBinService nowonClothingBinService,
            EunpyeongClothingBinService eunpyeongClothingBinService,
            EunpyeongBatteryBinService eunpyeongBatteryBinService,
            GangseoClothingBinService gangseoClothingBinService,
            GangseoBatteryBinService gangseoBatteryBinService,
            GeumcheonClothingBinService geumcheonClothingBinService,
            SeochoClothingBinService seochoClothingBinService,
            GangdongClothingBinService gangdongClothingBinService,
            GangdongMedicineBinService gangdongMedicineBinService
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
        this.yeongdeungpoClothingBinService = yeongdeungpoClothingBinService;
        this.yeongdeungpoBatteryBinService = yeongdeungpoBatteryBinService;
        this.gangnamClothingBinService = gangnamClothingBinService;
        this.gangnamBatteryBinService = gangnamBatteryBinService;
        this.seongbukClothingBinService = seongbukClothingBinService;
        this.seongbukBatteryBinService = seongbukBatteryBinService;
        this.jungnangBatteryBinService = jungnangBatteryBinService;
        this.jungnangClothingBinService = jungnangClothingBinService;
        this.yangcheonBatteryBinService = yangcheonBatteryBinService;
        this.yangcheonClothingBinService = yangcheonClothingBinService;
        this.yangcheonMedicineBinService = yangcheonMedicineBinService;
        this.songpaClothingBinService = songpaClothingBinService;
        this.songpaBatteryBinService = songpaBatteryBinService;
        this.songpaMedicineBinService = songpaMedicineBinService;
        this.guroClothingBinService = guroClothingBinService;
        this.mapoBatteryBinService = mapoBatteryBinService;
        this.mapoMedicineBinService = mapoMedicineBinService;
        this.seongdongClothingBinService = seongdongClothingBinService;
        this.seongdongBatteryBinService = seongdongBatteryBinService;
        this.gwangjinClothingBinService = gwangjinClothingBinService;
        this.gwangjinBatteryBinService = gwangjinBatteryBinService;
        this.dongdaemunClothingBinService = dongdaemunClothingBinService;
        this.dongdaemunBatteryBinService = dongdaemunBatteryBinService;
        this.dongdaemunMedicineBinService = dongdaemunMedicineBinService;
        this.gangbukClothingBinService = gangbukClothingBinService;
        this.dobongBatteryBinService = dobongBatteryBinService;
        this.nowonClothingBinService = nowonClothingBinService;
        this.eunpyeongClothingBinService = eunpyeongClothingBinService;
        this.eunpyeongBatteryBinService = eunpyeongBatteryBinService;
        this.gangseoClothingBinService = gangseoClothingBinService;
        this.gangseoBatteryBinService = gangseoBatteryBinService;
        this.geumcheonClothingBinService = geumcheonClothingBinService;
        this.seochoClothingBinService = seochoClothingBinService;
        this.gangdongClothingBinService = gangdongClothingBinService;
        this.gangdongMedicineBinService = gangdongMedicineBinService;

    }

    //지도 페이지 이동
    @GetMapping("/eco-map")
    public String map() {
        return "eco-map/recyclemap";
    }

    // api 출력을 도메인 구조에 맞게 변환
    // 종로구 의류수거함.
    @GetMapping("/api/clothing-bins")
    @ResponseBody
    public List<EcoLocation> getClothingBins() {
        return ecoLocationService.getLocationsByType("의류수거함");
    }
    // 종로구 폐건전지 수거함 데이터
    // 현재는 API 응답 DTO를 그대로 반환해서 테스트
    @GetMapping("/api/battery-bins")
    @ResponseBody
    public List<EcoLocation> getBatteryBins() {
        return ecoLocationService.getLocationsByType("폐건전지·폐형광등 수거함");
    }

    // 용산구 의류수거함 API 호출 및 DB 저장 테스트
    @PostMapping("/api/yongsan-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importYongsanClothingBins() {
        return yongsanClothingBinService.getClothingBins();
    }
    // 용산구 폐의약품 API 호출 및 DB 저장
    @PostMapping("/api/yongsan-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importYongsanMedicineBins() {
        return yongsanMedicineBinService.getMedicineBins();
    }

    // 폐의약품 수거함 DB 조회
    @GetMapping("/api/medicine-bins")
    @ResponseBody
    public List<EcoLocation> getMedicineBins() {
        return ecoLocationService.getLocationsByType("폐의약품 수거함");
    }

    //관악구 csv db에 저장
    @PostMapping("/api/gwanak-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGwanakClothingBins() {
        return gwanakClothingBinService.importClothingBins();
    }
    // 관악구 폐건전지·폐형광등 CSV DB 저장
    @PostMapping("/api/gwanak-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importGwanakBatteryBins() {
        return gwanakBatteryBinService.importBatteryBins();
    }
    // 관악구 폐의약품 CSV DB 저장
    @PostMapping("/api/gwanak-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importGwanakMedicineBins() {
        return gwanakMedicineBinService.importMedicineBins();
    }

    // 서대문구 폐의약품 API 호출 및 DB 저장 테스트
    @PostMapping("/api/seodaemun-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importSeodaemunMedicineBins() {
        return seodaemunMedicineBinService.getMedicineBins();
    }
    // 서대문구 의류수거함 API 호출 및 DB 저장
    @PostMapping("/api/seodaemun-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importSeodaemunClothingBins() {
        return seodaemunClothingBinService.getClothingBins();
    }

    //동작구 폐건전지, 폐형광등 api 저장
    @PostMapping("/api/dongjak-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importDongjakBatteryBins() {
        return dongjakBatteryBinService.getBatteryBins();
    }
    // 동작구 의류수거함 API 호출 및 DB 저장
    @PostMapping("/api/dongjak-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importDongjakClothingBins() {
        return dongjakClothingBinService.getClothingBins();
    }
    //동작구 폐의약품 api 호출 및 DB 저장
    @PostMapping("/api/dongjak-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importDongjakMedicineBins() {
        return dongjakMedicineBinService.getMedicineBins();
    }

    //영등포구 의류수거함 CSV 호출 및 DB에 저장
    @PostMapping("/api/yeongdeungpo-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importYeongdeungpoClothingBins() {
        return yeongdeungpoClothingBinService.importClothingBins();
    }
    //영등포구 폐건전지,폐형광등 CSV 호출 및 DB에 저장
    @PostMapping("/api/yeongdeungpo-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importYeongdeungpoBatteryBins() {
        return yeongdeungpoBatteryBinService.importBatteryBins();
    }

    //강남구 의류수거함 csv 호출 및 DB에 저장
    @PostMapping("/api/gangnam-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGangnamClothingBins() {
        return gangnamClothingBinService.importClothingBins();
    }
    // 강남구 폐건전지·폐형광등 CSV DB 저장
    @PostMapping("/api/gangnam-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importGangnamBatteryBins() {
        return gangnamBatteryBinService.importBatteryBins();
    }

    // 성북구 의류수거함 CSV 호출 및 DB에 저장
    @PostMapping("/api/seongbuk-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importSeongbukClothingBins() {
        return seongbukClothingBinService.importClothingBins();
    }
    // 성북구 폐건전지,폐형광등 api 호출 및 DB에 저장
    @PostMapping("/api/seongbuk-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importSeongbukBatteryBins() {
        return seongbukBatteryBinService.getBatteryBins();
    }

    // 중랑구 의류수거함 CSV 호출 및 DB에 저장
    @PostMapping("/api/jungnang-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importJungnangClothingBins(){
        return jungnangClothingBinService.importClothingBins();
    }
    // 중랑구 폐건전지, 폐형광등 api 호출 및 db에 저장
    @PostMapping("/api/jungnang-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importJungnangBatteryBins(){
        return jungnangBatteryBinService.getBatteryBins();
    }

    //양천구 의류수거함 api 호출 및 DB에 저장
    @PostMapping("/api/yangcheon-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importYangcheonClothingBins(){
        return yangcheonClothingBinService.getClothingBins();
    }
    // 양천구 폐건전지, 폐형광등 api 호출 및 DB에 저장
    @PostMapping("/api/yangcheon-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importYangcheonBatteryBins() { return yangcheonBatteryBinService.getBatteryBins();}
    // 양천구 폐의약품 수거함 api 호출 및 DB에 저장
    @PostMapping("/api/yangcheon-midicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importYangcheonMedicineBins() {
        return yangcheonMedicineBinService.getMedicineBins();
    }

    // 송파구 의류수거함 csv 호출 및 DB에 저장
    @PostMapping("/api/songpa-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importSongpaClothingBins(){
        return songpaClothingBinService.importClothingBins();
    }//송파구 폐건전지, 폐형광등 수거함 csv 호출 및 DB에 저장
    @PostMapping("/api/songpa-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importSongpaBatteryBins(){
        return songpaBatteryBinService.importBatteryBins();
    }// 송파구 폐의약품 수거함 api 호출 및 DB에 저장
    @PostMapping("/api/songpa-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importSongpaMedicineBins(){
        return songpaMedicineBinService.getMedicineBins();
    }

    // 구로구 의류수거함 CSV 호출 및 DB 저장
    @PostMapping("/api/guro-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGuroClothingBins() {
        return guroClothingBinService.importClothingBins();
    }

    // 마포구 폐건전지, 폐형광등 CSV 호출 및 DB 저장
    @PostMapping("/api/mapo-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importMapoBatteryBins(){
        return mapoBatteryBinService.importBatteryBins();
    }
    // 마포구 폐의약품 CSV 호출 및 DB 저장
    @PostMapping("/api/mapo-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importMapoMedicineBins(){
        return mapoMedicineBinService.importMedicineBins();
    }

    // 성동구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/seongdong-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importSeongdongClothingBins() {
        return seongdongClothingBinService.importClothingBins();
    }
    // 성동구 폐건전지, 폐형광등 CSV 호출 및 저장
    @PostMapping("/api/seongdong-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importSeongdongBatteryBins() {
        return seongdongBatteryBinService.importBatteryBins();
    }

    // 광진구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/gwangjin-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGwangjinClothingBins(){
        return gwangjinClothingBinService.importClothingBins();
    }
    // 광진구 폐건전지,폐형광등 csv 호출 및 저장
    @PostMapping("/api/gwangjin-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importGwangjinBatteryBins(){
        return gwangjinBatteryBinService.importBatteryBins();
    }

    // 동대문구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/dongdaemun-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importDongdaemunClothingBins(){
        return dongdaemunClothingBinService.importClothingBins();
    }
    // 동대문구 폐건전지,폐형광등 csv 호출 및 저장
    @PostMapping("/api/dongdaemun-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importDongdaemunBatteryBins() {
        return dongdaemunBatteryBinService.importBatteryBins();
    }
    // 동대문구 폐의약품 csv 호출 및 저장
    @PostMapping("/api/dongdaemun-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importDongdaemunMedicineBins() {
        return dongdaemunMedicineBinService.importMedicineBins();
    }

    // 강북구 의류수거함 api 호출 및 저장
    @PostMapping("/api/gangbuk-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGangbukClothingBins() {
        return gangbukClothingBinService.getClothingBins();
    }

    // 도봉구 폐건전지,폐형광등 api 호출 및 저장
    @PostMapping("/api/dobong-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importDobongBatteryBins() {
        return dobongBatteryBinService.getBatteryBins();
    }

    // 노원구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/nowon-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importNowonClothingBins() {
        return nowonClothingBinService.importClothingBins();
    }

    // 은평구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/eunpyeong-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importEunpyeongClothingBins(){
        return eunpyeongClothingBinService.importClothingBins();
    }
    // 은평구 폐건전지,폐형광등 csv 호출 및 저장
    @PostMapping("/api/eunpyeong-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importEunpyeongBatteryBins(){
        return eunpyeongBatteryBinService.importBatteryBins();
    }

    // 강서구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/gangseo-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGangseoClothingBins() {
        return gangseoClothingBinService.importClothingBins();
    }
    // 강서구 폐건전지,폐형광등 csv 호출 및 저장
    @PostMapping("/api/gangseo-battery-bins/import")
    @ResponseBody
    public List<EcoLocation> importGangseoBatteryBins() {
        return gangseoBatteryBinService.importBatteryBins();
    }

    // 금천구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/geumcheon-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGeumcheonClothingBins() {
        return geumcheonClothingBinService.importClothingBins();
    }

    // 서초구 의류수거함 api 호출 및 저장
    @PostMapping("/api/seocho-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importSeochoClothingBins() {
        return seochoClothingBinService.getClothingBins();
    }

    // 강동구 의류수거함 csv 호출 및 저장
    @PostMapping("/api/gangdong-clothing-bins/import")
    @ResponseBody
    public List<EcoLocation> importGangdongClothingBins() {
        return gangdongClothingBinService.importClothingBins();
    }
    // 강동구 폐의약품 csv 호출 및 저장
    @PostMapping("/api/gangdong-medicine-bins/import")
    @ResponseBody
    public List<EcoLocation> importGangdongMedicineBins() {
        return gangdongMedicineBinService.importMedicineBins();
    }


    // 폐가전 CSV DB 저장
    @PostMapping("/api/waste-electronics/import")
    @ResponseBody
    public List<EcoLocation> importWasteElectronics() {
        return wasteElectronicsService.importWasteElectronics();
    }
    // 폐가전 수거함 DB 조회
    @GetMapping("/api/waste-electronics")
    @ResponseBody
    public List<EcoLocation> getWasteElectronics() {
        return ecoLocationService.getLocationsByType(
                "폐가전 수거함");
    }



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