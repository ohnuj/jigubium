package first_project.recycle.service.mapservice.gwangjinservice;

import first_project.recycle.domain.EcoLocation;
import first_project.recycle.domain.ecoLocationdto.KakaoAddressResponse;
import first_project.recycle.mapper.EcoLocationMapper;
import first_project.recycle.service.KakaoAddressService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 광진구 폐건전지·폐형광등 수거함 CSV 데이터를 읽어서
 * 주소를 카카오 주소검색 API로 좌표 변환한 뒤
 * EcoLocation 객체로 만들어 DB에 저장하는 서비스
 *
 * 처리 흐름
 *
 * CSV 파일 읽기
 * ↓
 * 시설명 / 관할동 / 소재지 추출
 * ↓
 * KakaoAddressService로 주소 검색
 * ↓
 * 위도 / 경도 추출
 * ↓
 * EcoLocation 생성
 * ↓
 * EcoLocationMapper로 DB 저장
 * ↓
 * 저장 성공 목록 반환
 */
@Service
public class GwangjinBatteryBinService {

    // 주소를 위도 / 경도로 변환하기 위한 카카오 주소검색 서비스
    private final KakaoAddressService kakaoAddressService;

    // eco_location 테이블에 데이터를 저장하기 위한 MyBatis Mapper
    private final EcoLocationMapper ecoLocationMapper;


    /**
     * 생성자 주입
     *
     * Spring이 KakaoAddressService와
     * EcoLocationMapper 객체를 자동 주입함
     */
    public GwangjinBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    /**
     * 광진구 폐건전지·폐형광등 CSV 호출 및 DB 저장
     *
     * @return DB 저장에 성공한 수거함 목록
     */
    public List<EcoLocation> importBatteryBins() {

        // DB 저장이 성공한 데이터를 담아 반환할 리스트
        List<EcoLocation> savedList =
                new ArrayList<>();


        /*
         * resources 기준 CSV 파일 불러오기
         *
         * 실제 파일 위치:
         * src/main/resources/data/gwangjin-battery-bin.csv
         */
        ClassPathResource resource =
                new ClassPathResource(
                        "data/gwangjin-battery-bin.csv"
                );


        /*
         * 정리본 CSV는 UTF-8 인코딩이므로
         * StandardCharsets.UTF_8로 읽음
         */
        try (
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        resource.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            /*
             * 첫 번째 줄은 헤더이므로 건너뜀
             *
             * 구분,시설명,관할동,소재지,전화번호
             */
            br.readLine();


            String line;


            /*
             * CSV 데이터를 한 줄씩 끝까지 반복
             */
            while ((line = br.readLine()) != null) {


                // 빈 줄은 무시
                if (line.isBlank()) {
                    continue;
                }


                /*
                 * 쉼표 기준으로 컬럼 분리
                 *
                 * -1 옵션을 사용해서
                 * 전화번호처럼 빈 값이 있어도 컬럼 위치를 유지
                 */
                String[] data =
                        line.split(",", -1);


                /*
                 * CSV 컬럼 구조
                 *
                 * data[0] = 구분
                 * data[1] = 시설명
                 * data[2] = 관할동
                 * data[3] = 소재지
                 * data[4] = 전화번호
                 */
                if (data.length < 5) {

                    // 비정상 행은 건너뜀
                    continue;
                }


                /*
                 * 필요한 CSV 값 추출
                 */
                String facilityName =
                        data[1].trim();

                String adminDong =
                        data[2].trim();

                String roadAddress =
                        data[3].trim();


                /*
                 * 주소가 없으면
                 * 카카오 좌표 변환을 할 수 없으므로 건너뜀
                 */
                if (roadAddress.isBlank()) {
                    continue;
                }


                /*
                 * 카카오 주소검색 API 호출
                 *
                 * 소재지 주소를 이용해서
                 * 위도 / 경도를 구함
                 */
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                roadAddress
                        );


                /*
                 * 검색 결과가 없거나
                 * documents 배열이 비어있으면 좌표 변환 실패
                 *
                 * 해당 데이터는 DB에 저장하지 않고
                 * IntelliJ 콘솔에 주소를 출력
                 */
                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "광진구 폐건전지 좌표 변환 실패: "
                                    + roadAddress
                    );

                    continue;
                }


                /*
                 * 카카오 주소검색 결과 중
                 * 첫 번째 검색 결과 사용
                 */
                KakaoAddressResponse.Document document =
                        response
                                .getDocuments()
                                .get(0);


                /*
                 * 카카오 주소검색 API 좌표 기준
                 *
                 * x = 경도(longitude)
                 * y = 위도(latitude)
                 */
                BigDecimal latitude =
                        new BigDecimal(
                                document.getY()
                        );

                BigDecimal longitude =
                        new BigDecimal(
                                document.getX()
                        );


                /*
                 * DB 저장용 EcoLocation 객체 생성
                 */
                EcoLocation location =
                        new EcoLocation();


                /*
                 * 장소명
                 *
                 * 광진구 CSV의 시설명이 고유하게 존재하므로
                 * 시설명을 그대로 locationName에 저장
                 *
                 * 예:
                 * 폐형광등 폐건전지 수거함-28
                 */
                location.setLocationName(
                        facilityName
                );


                /*
                 * 장소 유형
                 *
                 * 원본 CSV의 구분은 "폐형광등 수거함"이지만
                 * 현재 프로젝트의 폐건전지 조회 API가
                 *
                 * getLocationsByType(
                 *     "폐건전지·폐형광등 수거함"
                 * )
                 *
                 * 구조이므로 기존 프로젝트 규칙에 맞춰 통일
                 */
                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );


                // 관할 행정동 저장
                location.setAdminDong(
                        adminDong
                );


                /*
                 * 정리본 CSV의 소재지를
                 * 도로명주소로 저장
                 */
                location.setRoadAddress(
                        roadAddress
                );


                /*
                 * 원본 데이터에 별도 지번주소 컬럼이 없으므로
                 * null 저장
                 */
                location.setJibunAddress(
                        null
                );


                // 카카오 API에서 변환한 위도 저장
                location.setLatitude(
                        latitude
                );


                // 카카오 API에서 변환한 경도 저장
                location.setLongitude(
                        longitude
                );


                /*
                 * MyBatis Mapper를 이용해서
                 * eco_location 테이블에 INSERT
                 */
                ecoLocationMapper
                        .insertEcoLocation(
                                location
                        );


                /*
                 * DB 저장 성공 데이터 목록에 추가
                 *
                 * 이후 컨트롤러 POST 결과에서
                 * 저장 개수를 확인할 수 있음
                 */
                savedList.add(
                        location
                );
            }


        } catch (Exception e) {

            /*
             * CSV 읽기 오류,
             * 카카오 API 처리 오류,
             * BigDecimal 변환 오류,
             * DB INSERT 오류 등을 콘솔에 출력
             */
            e.printStackTrace();
        }


        // DB 저장 성공한 전체 목록 반환
        return savedList;
    }
}
