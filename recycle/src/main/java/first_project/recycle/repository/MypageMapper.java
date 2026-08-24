package first_project.recycle.repository;

import first_project.recycle.domain.EcoPointHistory;
import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper
public interface MypageMapper {

    // memberId(회원ID(PK))로 회원 조회
    Member findById(@Param("memberId") Long memberId);

    // memberId로 기준으로 닉네임과 변경할 새 비밀번호를 동적으로 수정
    void updateMember(@Param("memberId") Long memberId,
                      @Param("info") MemberInfo memberinfo);

    // memberId로 회원 삭제
    void deleteById(@Param("memberId") Long memberId);

    // 회원의 현재 포인트 잔액 조회
    Integer findCurrentPointByMemberId(@Param("memberId") Long memberId);

    // 회원 상세 정보 조회 (findById와 유사한 용도)
    Member findMemberInfoById(@Param("memberId") Long memberId);

    // 회원이 작성한 총 게시글 수 조회
    int countBoardsById(@Param("memberId") Long MemberId);

    // 회원이 작성한 총 답글 수 조회
    int commentCountById(@Param("memberId") Long memberId);

    // 내 에코포인트 변동 내역 ArrayList 조회
    ArrayList<EcoPointHistory> findPointHistoryById(@Param("memberId") Long MemberId);

    // 구매한 리워드 상품명 및 갯수 조회
    List<Map<String, Object>> findMyRewardById(@Param("memberId") Long MemberId);

    // 회원 에코포인트 총 내역 건수 조회
    int countPointHistoryById(@Param("memberId") Long memberId);

    // 페이징 적용된 포인트 변동 내역 목록 조회 - 10개
    List<EcoPointHistory> findPointHistoryPagingById(
            @Param("memberId") Long memberId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
