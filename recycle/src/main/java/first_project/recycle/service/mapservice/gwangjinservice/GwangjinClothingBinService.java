package first_project.recycle.service.mapservice.gwangjinservice;
import first_project.recycle.domain.EcoLocation;
import first_project.recycle.mapper.EcoLocationMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 광진구 의류수거함 CSV 데이터를 읽어서
 * EcoLocation 객체로 변환한 뒤 DB에 저장하는 서비스
 *
 * 처리 흐름
 *
 * CSV 파일 읽기
 * ↓
 * 한 줄씩 데이터 분리
 * ↓
 * 위도 / 경도 BigDecimal 변환
 * ↓
 * EcoLocation 객체 생성
 * ↓
 * EcoLocationMapper를 통해 DB 저장
 * ↓
 * 저장된 데이터를 List로 반환
 *
 * 광진구 의류수거함 데이터는
 * 원본 CSV에 위도와 경도가 이미 포함되어 있으므로
 * KakaoAddressService를 이용한 주소 → 좌표 변환은 하지 않음
 */
@Service
public class GwangjinClothingBinService {

    // eco_location 테이블에 수거함 데이터를 저장하기 위한 MyBatis Mapper
    private final EcoLocationMapper ecoLocationMapper;


    /**
     * 생성자 주입
     *
     * Spring이 EcoLocationMapper 객체를 주입해줌
     */
    public GwangjinClothingBinService(
            EcoLocationMapper ecoLocationMapper
    ) {
        this.ecoLocationMapper = ecoLocationMapper;
    }


    /**
     * 광진구 의류수거함 CSV 호출 및 DB 저장
     *
     * src/main/resources/data/gwangjin-clothing-bin.csv
     * 파일을 읽어 한 행씩 EcoLocation 객체로 변환하고
     * eco_location 테이블에 저장함
     *
     * @return DB 저장에 성공한 의류수거함 목록
     */
    public List<EcoLocation> importClothingBins() {

        // DB 저장이 완료된 데이터를 담을 리스트
        // 컨트롤러에서 import 결과 확인용으로 반환
        List<EcoLocation> savedList =
                new ArrayList<>();


        /*
         * resources 폴더 안의 CSV 파일 불러오기
         *
         * 실제 위치:
         * src/main/resources/data/gwangjin-clothing-bin.csv
         *
         * ClassPathResource는 resources 폴더를 기준으로 파일을 찾음
         */
        ClassPathResource resource =
                new ClassPathResource(
                        "data/gwangjin-clothing-bin.csv"
                );


        /*
         * CSV 파일 읽기
         *
         * 정리한 CSV 파일은 UTF-8 형식이므로
         * StandardCharsets.UTF_8 사용
         *
         * try-with-resources를 사용하여
         * 파일 읽기가 끝나면 BufferedReader가 자동으로 닫힘
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
             * 첫 번째 줄은 컬럼명(헤더)이므로 건너뜀
             *
             * 행정동,지번주소,도로명주소,위도,경도,기준일
             */
            br.readLine();


            String line;


            /*
             * CSV 파일의 데이터를
             * 한 줄씩 끝까지 반복해서 읽음
             */
            while ((line = br.readLine()) != null) {


                // 빈 줄이 존재할 경우 처리하지 않고 다음 줄로 이동
                if (line.isBlank()) {
                    continue;
                }


                /*
                 * 쉼표(,)를 기준으로 한 행의 데이터를 분리
                 *
                 * -1 옵션을 사용하면
                 * 도로명주소처럼 값이 비어 있는 컬럼도 유지됨
                 *
                 * 예:
                 *
                 * "광장동,서울특별시 광진구 광장동 123,,37.123,127.123,2026-03-04"
                 *
                 * 도로명주소가 비어 있어도
                 * data[2]가 사라지지 않음
                 */
                String[] data =
                        line.split(",", -1);


                /*
                 * CSV 컬럼 구조
                 *
                 * data[0] = 행정동
                 * data[1] = 지번주소
                 * data[2] = 도로명주소
                 * data[3] = 위도
                 * data[4] = 경도
                 * data[5] = 기준일
                 */
                if (data.length < 6) {

                    // 비정상적인 행은 저장하지 않고 건너뜀
                    continue;
                }


                /*
                 * CSV 각 컬럼 값 추출
                 *
                 * trim()으로 앞뒤 불필요한 공백 제거
                 */
                String adminDong =
                        data[0].trim();

                String jibunAddress =
                        data[1].trim();

                String roadAddress =
                        data[2].trim();

                String latitudeText =
                        data[3].trim();

                String longitudeText =
                        data[4].trim();


                /*
                 * 기준일 data[5]는
                 * 현재 EcoLocation 테이블에 저장할 컬럼이 없으므로
                 * DB 저장에는 사용하지 않음
                 */


                /*
                 * 위도 또는 경도가 없는 데이터는
                 * 지도에 마커를 표시할 수 없으므로 저장하지 않음
                 *
                 * 현재 정리된 광진구 CSV에는
                 * 모든 행에 좌표가 존재하지만
                 * 예외 상황을 대비한 방어 코드
                 */
                if (
                        latitudeText.isBlank() ||
                                longitudeText.isBlank()
                ) {
                    continue;
                }


                /*
                 * CSV에서 읽은 좌표는 String 타입이므로
                 * EcoLocation의 좌표 타입인 BigDecimal로 변환
                 */
                BigDecimal latitude =
                        new BigDecimal(
                                latitudeText
                        );

                BigDecimal longitude =
                        new BigDecimal(
                                longitudeText
                        );


                /*
                 * DB에 저장할 EcoLocation 객체 생성
                 */
                EcoLocation location =
                        new EcoLocation();


                /*
                 * 장소명
                 *
                 * 광진구 CSV에는 별도의 수거함 이름이 없으므로
                 * "의류수거함"으로 통일
                 */
                location.setLocationName(
                        "의류수거함"
                );


                /*
                 * 장소 유형
                 *
                 * 지도에서 의류수거함 데이터를 조회할 때
                 *
                 * getLocationsByType("의류수거함")
                 *
                 * 으로 조회하므로 동일한 문자열로 저장
                 */
                location.setLocationType(
                        "의류수거함"
                );


                // CSV의 행정동 저장
                location.setAdminDong(
                        adminDong
                );


                /*
                 * 도로명주소 저장
                 *
                 * 광진구 데이터는 일부 행에
                 * 도로명주소가 존재하지 않음
                 *
                 * 빈 문자열을 그대로 저장하지 않고
                 * 값이 없으면 null로 저장
                 */
                if (roadAddress.isBlank()) {

                    location.setRoadAddress(
                            null
                    );

                } else {

                    location.setRoadAddress(
                            roadAddress
                    );
                }


                /*
                 * 지번주소 저장
                 *
                 * 정리된 CSV에서는 모든 행에
                 * 지번주소가 존재함
                 */
                location.setJibunAddress(
                        jibunAddress
                );


                /*
                 * CSV에 이미 제공된 위도 / 경도 저장
                 *
                 * 주소 변환 API를 호출하지 않고
                 * 제공된 좌표를 그대로 사용
                 */
                location.setLatitude(
                        latitude
                );

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
                 * DB 저장이 완료된 객체를 리스트에 추가
                 *
                 * 이후 컨트롤러에서 POST 요청 결과로
                 * 저장된 데이터 목록을 확인할 수 있음
                 */
                savedList.add(
                        location
                );
            }


        } catch (Exception e) {

            /*
             * CSV 파일 읽기 오류,
             * 좌표 변환 오류,
             * DB INSERT 오류 등이 발생하면
             * IntelliJ 콘솔에서 확인할 수 있도록 출력
             */
            e.printStackTrace();
        }


        // DB 저장에 성공한 데이터 목록 반환
        return savedList;
    }
}
