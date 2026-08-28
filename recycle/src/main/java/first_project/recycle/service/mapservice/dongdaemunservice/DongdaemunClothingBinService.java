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
 * 동대문구 의류수거함 CSV 데이터를 읽어서
 * 주소를 카카오 주소검색 API로 좌표 변환한 뒤
 * EcoLocation 객체로 만들어 DB에 저장하는 서비스
 *
 * 처리 흐름
 *
 * CSV 파일 읽기
 * ↓
 * 행정동 / 주소 추출
 * ↓
 * KakaoAddressService를 이용해 주소 → 좌표 변환
 * ↓
 * EcoLocation 객체 생성
 * ↓
 * EcoLocationMapper를 통해 DB 저장
 * ↓
 * 저장에 성공한 목록 반환
 *
 * 동대문구 의류수거함 CSV에는
 * 위도와 경도가 제공되지 않기 때문에
 * 카카오 주소검색 API를 사용함
 */
@Service
public class DongdaemunClothingBinService {

    // 주소를 위도 / 경도로 변환하기 위한 서비스
    private final KakaoAddressService kakaoAddressService;

    // eco_location 테이블 DB 저장용 MyBatis Mapper
    private final EcoLocationMapper ecoLocationMapper;


    /**
     * 생성자 주입
     */
    public DongdaemunClothingBinService(
            KakaoAddressService kakaoAddressService,
            EcoLocationMapper ecoLocationMapper
    ) {
        this.kakaoAddressService = kakaoAddressService;
        this.ecoLocationMapper = ecoLocationMapper;
    }


    /**
     * 동대문구 의류수거함 CSV 호출 및 DB 저장
     *
     * 파일 위치:
     * src/main/resources/data/dongdaemun-clothing-bin.csv
     *
     * @return DB 저장에 성공한 의류수거함 목록
     */
    public List<EcoLocation> importClothingBins() {

        // DB 저장에 성공한 데이터 목록
        List<EcoLocation> savedList =
                new ArrayList<>();


        /*
         * resources 폴더를 기준으로
         * 정리한 동대문구 CSV 파일 불러오기
         */
        ClassPathResource resource =
                new ClassPathResource(
                        "data/dongdaemun-clothing-bin.csv"
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
             * 연번,행정동,주소,상세위치,데이터 기준일
             */
            br.readLine();


            String line;


            /*
             * CSV 데이터를 한 줄씩 끝까지 반복해서 읽음
             */
            while ((line = br.readLine()) != null) {

                // 빈 줄은 처리하지 않음
                if (line.isBlank()) {
                    continue;
                }


                /*
                 * 쉼표 기준으로 CSV 컬럼 분리
                 *
                 * -1을 사용해서 빈 컬럼이 존재해도
                 * 배열 위치가 유지되도록 함
                 */
                String[] data =
                        line.split(",", -1);


                /*
                 * CSV 컬럼 구조
                 *
                 * data[0] = 연번
                 * data[1] = 행정동
                 * data[2] = 주소
                 * data[3] = 상세위치
                 * data[4] = 데이터 기준일
                 */
                if (data.length < 5) {
                    continue;
                }


                /*
                 * DB 저장 및 좌표 변환에 필요한 값 추출
                 */
                String adminDong =
                        data[1].trim();

                String address =
                        data[2].trim();


                /*
                 * 상세위치와 데이터 기준일은
                 * 현재 EcoLocation에 대응되는 컬럼이 없으므로
                 * DB 저장에는 사용하지 않음
                 */


                /*
                 * 주소가 없으면 카카오 주소검색을 할 수 없으므로
                 * 해당 데이터는 건너뜀
                 */
                if (address.isBlank()) {
                    continue;
                }


                /*
                 * 카카오 주소검색 API 호출
                 *
                 * 정리본 CSV 주소는
                 * "서울특별시 동대문구 ..."
                 * 형태로 정규화되어 있음
                 */
                KakaoAddressResponse response =
                        kakaoAddressService.searchAddress(
                                address
                        );


                /*
                 * 주소 검색 결과가 없으면
                 * 좌표를 만들 수 없으므로 DB 저장하지 않음
                 *
                 * 실패 주소는 IntelliJ 콘솔에서 확인 가능
                 */
                if (
                        response == null ||
                                response.getDocuments() == null ||
                                response.getDocuments().isEmpty()
                ) {

                    System.out.println(
                            "동대문구 의류수거함 좌표 변환 실패: "
                                    + address
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
                 * x = 경도
                 * y = 위도
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
                 * DB 저장용 객체 생성
                 */
                EcoLocation location =
                        new EcoLocation();


                /*
                 * 의류수거함은 기존 프로젝트 정책대로
                 * 장소명과 장소유형을 동일하게 통일
                 */
                location.setLocationName(
                        "의류수거함"
                );

                location.setLocationType(
                        "의류수거함"
                );


                // CSV의 행정동 저장
                location.setAdminDong(
                        adminDong
                );


                /*
                 * 주소 종류 구분
                 *
                 * 대부분 도로명주소지만
                 * 정리본에 "장안동 348"처럼
                 * 지번주소 형식의 데이터가 1건 존재함
                 *
                 * "동 + 숫자" 형태면 지번주소로 저장하고
                 * 나머지는 도로명주소로 저장
                 */
                if (
                        address.matches(
                                ".*[가-힣]+동\\s+\\d+(?:-\\d+)?$"
                        )
                ) {

                    location.setRoadAddress(null);
                    location.setJibunAddress(address);

                } else {

                    location.setRoadAddress(address);
                    location.setJibunAddress(null);
                }


                // 카카오 API에서 변환한 위도 저장
                location.setLatitude(
                        latitude
                );

                // 카카오 API에서 변환한 경도 저장
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
                 * DB 저장 성공 목록에 추가
                 *
                 * 이후 POST 요청 결과에서
                 * 실제 저장 성공 건수를 확인할 수 있음
                 */
                savedList.add(
                        location
                );
            }


        } catch (Exception e) {

            /*
             * CSV 읽기 오류
             * 카카오 API 오류
             * BigDecimal 변환 오류
             * DB INSERT 오류 등을 콘솔에 출력
             */
            e.printStackTrace();
        }


        // DB 저장에 성공한 전체 데이터 반환
        return savedList;
    }
}