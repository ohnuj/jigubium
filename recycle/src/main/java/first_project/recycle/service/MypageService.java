package first_project.recycle.service;

import first_project.recycle.domain.EcoPointHistory;
import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import first_project.recycle.repository.EcoPointHistoryMapper;
import first_project.recycle.repository.MypageMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MypageService {

    private final MypageMapper mypageMapper;

    // 회원 정보 조회
    public Member getMemberInfo(Long memberId) {
        return mypageMapper.findById(memberId);
    }

    // 회원 정보 수정
    @Transactional
    public void updateMemberInfo(Long memberId, MemberInfo memberinfo) {
        mypageMapper.updateMember(memberId, memberinfo);
    }
    // 비밀번호 확인 절차
    public boolean verifyPassword(Long memberId, String password) {
        Member member = mypageMapper.findById(memberId);
        if (member == null || member.getPassword() == null) {
            return false;
        }
        return member.getPassword().equals(password);
    }

    // 회원 탈퇴
    @Transactional
    public boolean deleteMember(Long memberId, String password) {
        Member member = mypageMapper.findById(memberId);
        if (member == null || !password.equals(member.getPassword())) {
            return false;
        }
        mypageMapper.deleteById(memberId);
        return true;
    }

    // 작성한 게시글 수
    public int getBoardCount(Long memberId){
        return mypageMapper.countBoardsById(memberId);
    }

    // 포인트 변동 내역 목록
    public ArrayList<EcoPointHistory> getPointHistory(Long memberId){
        return mypageMapper.findPointHistoryById(memberId);
    }

    // 보유 리워드 품목 및 수량 조회
    public List<Map<String, Object>> getMyReward(Long memberId) {
        return mypageMapper.findMyRewardById(memberId);
    }

    // 포인트 변동내역 총 개수 조회
    public int getPointPageCount(Long memberId) {
        return mypageMapper.countPointHistoryById(memberId);
    }

    // 10개씩 페이징 처리된 포인트 내역 조회
    public List<EcoPointHistory> getPointHistoryPaging(Long memberId, int page, int pageSize){
        int offset = (page - 1) * pageSize;
        return mypageMapper.findPointHistoryPagingById(memberId, offset, pageSize);
    }
}
