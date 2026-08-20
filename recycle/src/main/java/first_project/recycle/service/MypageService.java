package first_project.recycle.service;

import first_project.recycle.domain.Member;
import first_project.recycle.domain.MemberInfo;
import first_project.recycle.repository.MypageMapper;
import first_project.recycle.repository.MypageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MypageService {
    private final MypageMapper mypageMapper; // 또는 MemberMapper

    // 회원정보 조회
    public Member getMemberInfo(String loginEmail){
        return mypageMapper.findByEmail(loginEmail);
    }

    public void updateMemberInfo(String loginEmail, MemberInfo memberinfo){
        mypageMapper.updateMember(loginEmail,memberinfo);
    }

    // 비밀번호 검증
    public boolean verifyPassword(String loginEmail, String password) {
        Member member = mypageMapper.findByEmail(loginEmail);
        if (member == null) {
            return false;
        }
        return member.getPassword().equals(password);
    }

    // 회원 삭제 (조회 후 삭제 또는 바로 삭제)
    public boolean deleteMember(String email, String password) {
        Member member = mypageMapper.findByEmail(email);
        if (member == null || ! password.equals(member.getPassword())) {
            return false;
        }
        mypageMapper.deleteByEmail(email);
        return true;
    }
}
