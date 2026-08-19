package first_project.recycle.mapper;

import first_project.recycle.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// MyBatis 매퍼 인터페이스임을 명시하여 스프링 컨테이너 빈으로 등록
@Mapper
public interface UserMapper {

    // 신규 회원 정보 삽입 (UserMapper.xml의 id="insert"와 매핑)
    void insert(User user);

    // 이메일로 회원 단건 조회 (UserMapper.xml의 id="findByEmail"과 매핑)
    // @Param: XML 매퍼 쿼리 내 #{email} 파라미터와 매핑
    User findByEmail(@Param("email") String email);

    // 동일한 이메일을 가진 회원 수 카운트 (UserMapper.xml의 id="countByEmail"과 매핑)
    // @Param: XML 매퍼 쿼리 내 #{email} 파라미터와 매핑
    int countByEmail(@Param("email") String email);
}