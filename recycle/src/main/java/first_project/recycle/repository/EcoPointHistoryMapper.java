package first_project.recycle.repository;

import first_project.recycle.domain.EcoPointHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EcoPointHistoryMapper {
    int insertPointHistory(EcoPointHistory ecoPointHistory);
    //리워드 구매 전, 마이페이지
    int findCurrentBalance(@Param("memberId") Long memberId);
    //뱃지
    int findTotalPoint(@Param("memberId") Long memberId);
    //마이페이지 포인트 내역
    List<EcoPointHistory> findByMemberId(@Param("memberId") Long memberId);
}
