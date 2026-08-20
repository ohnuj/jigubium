package first_project.recycle.service;

import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import first_project.recycle.repository.EcoPointHistoryMapper;
import first_project.recycle.repository.MypageMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

}