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
 * 동대문구 폐의약품 수거함 CSV 데이터를 읽어서
 * 카카오 주소검색 API를 통해 좌표를 변환한 뒤
 * eco_location 테이블에 저장하는 서비스
 *
 * 처리 흐름
 *
 * CSV 파일 읽기
 * ↓
 * 명칭 / 행정동 / 주소 추출
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
 * 동대문구 폐의약품 데이터에는
 * 위도 / 경도가 제공되지 않으므로
 * 주소를 이용해 좌표를 직접 구해야 함
 */
@Service
public class DongdaemunMedicineBinService {

    // 주소를 위도 / 경도로 변환하기 위한 카카오 주소검색 서비스
    private final KakaoAddressService kakaoAddressService;

    // eco_location 테이블 저장용 MyBatis Mapper
    private final EcoLocationMapper ecoLocationMapper;


    /**
     * 생성자 주입
     */
    public DongdaemunMedicineBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    /**
     * 동대문구 폐의약품 CSV 호출 및 DB 저장
     *
     * CSV 파일 위치:
     * src/main/resources/data/dongdaemun-medicine-bin.csv
     *
     * @return DB 저장에 성공한 폐의약품 수거함 목록
     */
    public List<EcoLocation> importMedicineBins() {

        // DB 저장 성공 데이터를 담을 리스트
        List<EcoLocation> savedList =
                new ArrayList<>();


        /*
         * resources 폴더 기준으로
         * 정리한 CSV 파일 불러오기
         */
        ClassPathResource resource =
                new ClassPathResource(
                        "data/dongdaemun-medicine-bin.csv"
                );


        /*
         * 정리본 CSV는 UTF-8 형식이므로
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
             * 번호,명칭,행정동,주소,상세위치,연락처
             */
            br.readLine();


            String line;


            /*
             * CSV 파일을 한 줄씩 끝까지 읽음
             */
            while ((line = br.readLine()) != null) {

                // 빈 줄은 처리하지 않음
                if (line.isBlank()) {
                    continue;
                }


                /*
                 * 쉼표 기준으로 컬럼 분리
                 *
                 * 정리 과정에서 상세위치 내부의 쉼표는
                 * "/" 형태로 변경했기 때문에
                 * split(",", -1) 방식으로 안전하게 읽을 수 있음
                 */
                String[] data =
                        line.split(",", -1);


                /*
                 * CSV 컬럼 구조
                 *
                 * data[0] = 번호
                 * data[1] = 명칭
                 * data[2] = 행정동
                 * data[3] = 주소
                 * data[4] = 상세위치
                 * data[5] = 연락처
                 */
                if (data.length < 6) {
                    continue;
                }


                /*
                 * DB 저장에 필요한 데이터 추출
                 */
                String facilityName =
                        data[1].trim();

                String adminDong =
                        data[2].trim();

                String roadAddress =
                        data[3].trim();

                String detail =
                        data[4].trim();


                /*
                 * 연락처는 현재 EcoLocation에
                 * 대응되는 필드가 없으므로 저장하지 않음
                 *
                 * 상세위치 역시 별도 컬럼이 없기 때문에
                 * 현재는 DB에 별도로 저장하지 않음
                 */


                /*
                 * 주소가 없으면
                 * 카카오 주소 검색을 사용할 수 없으므로 건너뜀
                 */
                if (roadAddress.isBlank()) {
                    continue;
                }


                /*
                 * 카카오 주소검색 API 호출
                 *
                 * 정리본 CSV의 주소는
                 * 건물명 / 층 등의 상세정보를 제거하고
                 * 주소 검색에 사용할 수 있는 형태로 정리되어 있음
                 */
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                roadAddress
                        );


                /*
                 * 카카오 주소검색 결과가 없는 경우
                 * 좌표를 만들 수 없으므로 DB 저장하지 않음
                 *
                 * 실패 주소는 IntelliJ 콘솔에 출력
                 */
                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "동대문구 폐의약품 좌표 변환 실패: "
                                    + roadAddress
                    );

                    continue;
                }


                /*
                 * 검색 결과 중 첫 번째 주소 사용
                 */
                KakaoAddressResponse.Document document =
                        response
                                .getDocuments()
                                .get(0);


                /*
                 * 카카오 주소 API 좌표
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
                 * 원본 CSV에 실제 기관 / 수거장소 명칭이 있으므로
                 * 해당 명칭을 그대로 locationName으로 사용
                 */
                if (facilityName.isBlank()) {

                    location.setLocationName(
                            "폐의약품 수거함"
                    );

                } else {

                    location.setLocationName(
                            facilityName
                    );
                }


                /*
                 * 장소 유형
                 *
                 * 현재 지도 조회 API의 폐의약품 유형과
                 * 동일한 문자열을 사용
                 */
                location.setLocationType(
                        "폐의약품 수거함"
                );


                /*
                 * 정리본 CSV에서 분리한 행정동 저장
                 */
                if (adminDong.isBlank()) {

                    location.setAdminDong(
                            null
                    );

                } else {

                    location.setAdminDong(
                            adminDong
                    );
                }


                /*
                 * 정리한 주소를 도로명주소로 저장
                 */
                location.setRoadAddress(
                        roadAddress
                );


                /*
                 * 별도의 지번주소 데이터가 제공되지 않으므로
                 * null 저장
                 */
                location.setJibunAddress(
                        null
                );


                // 카카오 API에서 가져온 위도 저장
                location.setLatitude(
                        latitude
                );


                // 카카오 API에서 가져온 경도 저장
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
                 * DB 저장 성공한 객체를 리스트에 추가
                 */
                savedList.add(
                        location
                );
            }


        } catch (Exception e) {

            /*
             * CSV 파일 읽기 오류
             * 카카오 API 오류
             * 좌표 변환 오류
             * DB INSERT 오류 등을 콘솔에 출력
             */
            e.printStackTrace();
        }


        // DB 저장 성공 목록 반환
        return savedList;
    }
}