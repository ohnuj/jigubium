package first_project.recycle.repository;

import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MypageMapper {

    // 이메일로 회원조회
    Member findByEmail(@Param("email") String email);

    // 회원정보 수정
    void updateMember(@Param("email") String email,
                      @Param("info") MemberInfo memberinfo);

    // 이메일로 회원 삭제
   void deleteByEmail(String email);
}
