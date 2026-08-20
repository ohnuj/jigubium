package first_project.recycle.repository;

import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MypageMapper {

    // memberId로 회원 조회
    Member findById(@Param("memberId") Long memberId);

    // memberId로 회원 정보 수정
    void updateMember(@Param("memberId") Long memberId,
                      @Param("info") MemberInfo memberinfo);

    // memberId로 회원 삭제
    void deleteById(@Param("memberId") Long memberId);

    // 회원의 현재 포인트 잔액 조회
    Integer findCurrentPointByMemberId(@Param("memberId") Long memberId);
    Member findMemberInfoById(@Param("memberId") Long memberId);
}
