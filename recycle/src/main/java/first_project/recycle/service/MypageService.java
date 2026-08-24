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

    // DB 접근을 담당하는 매퍼 인터페이스 주입
    private final MypageMapper mypageMapper;

    // 회원 정보 조회
    public Member getMemberInfo(Long memberId) {
        return mypageMapper.findById(memberId);
    }

    // 회원 정보 수정
    @Transactional // DB수정 작업 중 예외가 터지면 롤백하여 데이터 무결성 보장
    public void updateMemberInfo(Long memberId, MemberInfo memberinfo) {
        mypageMapper.updateMember(memberId, memberinfo);
    }
    // 비밀번호 확인 절차 -> DB의 기존 비밀번호와 사용자가 입력창에 적은 문자열이 같은지 비교
    public boolean verifyPassword(Long memberId, String password) {
        Member member = mypageMapper.findById(memberId);
        if (member == null || member.getPassword() == null) {
            return false;
        }
        // 문지 동등 비교 결과 반환
        return member.getPassword().equals(password);
    }

    // 회원 탈퇴
    // 입력한 비밀번호 검증 -> 일치 시 DB행 삭제
    @Transactional
    public boolean deleteMember(Long memberId, String password) {
        Member member = mypageMapper.findById(memberId);
        if (member == null || !password.equals(member.getPassword())) {
            return false;
        }
        // DB에서 회원 행 삭제 실행
        mypageMapper.deleteById(memberId);
        return true;
    }

    // 작성한 게시글 수
    public int getBoardCount(Long memberId){
        return mypageMapper.countBoardsById(memberId);
    }

    // 작성한 댓글 수
    public int getCommentCount(Long memberId) { return mypageMapper.commentCountById(memberId); }

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
