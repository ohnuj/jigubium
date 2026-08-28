package first_project.recycle.service.mapservice.dongdaemunservice;
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
 * 동대문구 폐건전지·폐형광등 수거함 CSV 데이터를 읽어서
 * 카카오 주소 검색 API를 통해 좌표를 변환하고
 * EcoLocation 객체로 만들어 DB에 저장하는 서비스
 *
 * 처리 흐름
 *
 * CSV 파일 읽기
 * ↓
 * 설치주소 / 설치건물 정보 추출
 * ↓
 * KakaoAddressService로 주소 검색
 * ↓
 * 위도 / 경도 변환
 * ↓
 * EcoLocation 객체 생성
 * ↓
 * EcoLocationMapper로 DB 저장
 * ↓
 * 저장 성공 목록 반환
 *
 * 동대문구 폐건전지·폐형광등 CSV에는
 * 위도와 경도가 제공되지 않으므로
 * 주소를 이용해 좌표를 직접 구해야 함
 */
@Service
public class DongdaemunBatteryBinService {

    // 주소를 위도 / 경도로 변환하기 위한 카카오 주소 검색 서비스
    private final KakaoAddressService kakaoAddressService;

    // eco_location 테이블에 데이터를 저장하기 위한 MyBatis Mapper
    private final EcoLocationMapper ecoLocationMapper;


    /**
     * 생성자 주입
     *
     * Spring이 KakaoAddressService와
     * EcoLocationMapper 객체를 자동으로 주입함
     */
    public DongdaemunBatteryBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    /**
     * 동대문구 폐건전지·폐형광등 CSV 호출 및 DB 저장
     *
     * 실제 CSV 위치:
     * src/main/resources/data/dongdaemun-battery-bin.csv
     *
     * @return DB 저장에 성공한 수거함 목록
     */
    public List<EcoLocation> importBatteryBins() {

        // DB 저장에 성공한 데이터를 담아 반환할 리스트
        List<EcoLocation> savedList =
                new ArrayList<>();


        /*
         * resources 폴더를 기준으로 CSV 파일 불러오기
         */
        ClassPathResource resource =
                new ClassPathResource(
                        "data/dongdaemun-battery-bin.csv"
                );


        /*
         * 정리본 CSV는 UTF-8 형식이므로
         * StandardCharsets.UTF_8을 사용해 읽음
         *
         * try-with-resources를 사용하므로
         * 파일 처리가 끝나면 BufferedReader가 자동으로 닫힘
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
             * 첫 번째 줄은 CSV 헤더이므로 건너뜀
             *
             * 설치주소,
             * 설치건물,
             * 보유수량,
             * 관리부서명,
             * 관리부서 전화번호,
             * 데이터기준일자
             */
            br.readLine();


            String line;


            /*
             * CSV 데이터를 한 줄씩 끝까지 반복해서 읽음
             */
            while ((line = br.readLine()) != null) {

                // 빈 줄은 처리하지 않고 다음 데이터로 이동
                if (line.isBlank()) {
                    continue;
                }


                /*
                 * 쉼표(,)를 기준으로 컬럼 분리
                 *
                 * -1 옵션을 사용하면
                 * 설치건물처럼 값이 비어 있는 컬럼도
                 * 배열 위치가 유지됨
                 */
                String[] data =
                        line.split(",", -1);


                /*
                 * CSV 컬럼 구조
                 *
                 * data[0] = 설치주소
                 * data[1] = 설치건물
                 * data[2] = 보유수량
                 * data[3] = 관리부서명
                 * data[4] = 관리부서 전화번호
                 * data[5] = 데이터기준일자
                 */
                if (data.length < 6) {
                    continue;
                }


                /*
                 * DB 저장 및 좌표 변환에 필요한 데이터 추출
                 */
                String roadAddress =
                        data[0].trim();

                String buildingName =
                        data[1].trim();


                /*
                 * 보유수량(data[2]),
                 * 관리부서명(data[3]),
                 * 관리부서 전화번호(data[4]),
                 * 데이터기준일자(data[5])는
                 *
                 * 현재 EcoLocation에 대응되는 필드가 없으므로
                 * DB 저장에는 사용하지 않음
                 */


                /*
                 * 주소가 없으면
                 * 카카오 주소 검색을 사용할 수 없기 때문에
                 * 해당 행은 저장하지 않음
                 */
                if (roadAddress.isBlank()) {
                    continue;
                }


                /*
                 * 카카오 주소 검색 API 호출
                 *
                 * 정리본 CSV 주소는
                 * "서울특별시 동대문구 ..."
                 * 형태로 정규화되어 있음
                 */
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                roadAddress
                        );


                /*
                 * 카카오 주소 검색 결과 확인
                 *
                 * 응답 자체가 없거나
                 * documents가 없거나
                 * 검색 결과가 0건이면 좌표 변환 실패
                 *
                 * 해당 행은 DB에 넣지 않고
                 * 실패한 주소를 IntelliJ 콘솔에 출력
                 */
                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "동대문구 폐건전지 좌표 변환 실패: "
                                    + roadAddress
                    );

                    continue;
                }


                /*
                 * 검색 결과 중 첫 번째 주소 결과 사용
                 */
                KakaoAddressResponse.Document document =
                        response
                                .getDocuments()
                                .get(0);


                /*
                 * 카카오 주소 검색 API 좌표
                 *
                 * x = 경도(longitude)
                 * y = 위도(latitude)
                 *
                 * EcoLocation의 좌표 타입이 BigDecimal이므로
                 * String 값을 BigDecimal로 변환
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
                 * DB에 저장할 EcoLocation 객체 생성
                 */
                EcoLocation location =
                        new EcoLocation();


                /*
                 * 장소명 설정
                 *
                 * 설치건물 정보가 존재하면
                 * 실제 장소명을 사용자에게 보여주는 것이 좋으므로
                 * 설치건물명을 locationName으로 사용
                 *
                 * 설치건물이 비어 있는 경우에는
                 * 기본 명칭을 사용
                 */
                if (buildingName.isBlank()) {

                    location.setLocationName(
                            "폐건전지·폐형광등 수거함"
                    );

                } else {

                    location.setLocationName(
                            buildingName
                    );
                }


                /*
                 * 장소 유형
                 *
                 * 기존 지도 조회 API가
                 *
                 * getLocationsByType(
                 *     "폐건전지·폐형광등 수거함"
                 * )
                 *
                 * 방식으로 조회하므로
                 * 동일한 문자열로 통일하여 저장
                 */
                location.setLocationType(
                        "폐건전지·폐형광등 수거함"
                );


                /*
                 * 원본 CSV에는 행정동 컬럼이 없음
                 *
                 * 임의로 주소에서 추측하지 않고
                 * null로 저장
                 */
                location.setAdminDong(
                        null
                );


                /*
                 * 정리본의 설치주소를
                 * 도로명주소로 저장
                 */
                location.setRoadAddress(
                        roadAddress
                );


                /*
                 * 별도의 지번주소 컬럼이 제공되지 않으므로
                 * null 저장
                 */
                location.setJibunAddress(
                        null
                );


                // 카카오 주소 검색에서 구한 위도 저장
                location.setLatitude(
                        latitude
                );


                // 카카오 주소 검색에서 구한 경도 저장
                location.setLongitude(
                        longitude
                );


                /*
                 * MyBatis Mapper를 통해
                 * eco_location 테이블에 INSERT
                 */
                ecoLocationMapper
                        .insertEcoLocation(
                                location
                        );


                /*
                 * DB 저장에 성공한 객체를 리스트에 추가
                 *
                 * 컨트롤러에서 POST 요청 결과로
                 * 실제 저장 성공 건수를 확인할 수 있음
                 */
                savedList.add(
                        location
                );
            }


        } catch (Exception e) {

            /*
             * CSV 파일 읽기 오류
             * Kakao API 처리 오류
             * BigDecimal 변환 오류
             * DB INSERT 오류 등을
             * IntelliJ 콘솔에서 확인할 수 있도록 출력
             */
            e.printStackTrace();
        }


        // DB 저장에 성공한 전체 목록 반환
        return savedList;
    }
}