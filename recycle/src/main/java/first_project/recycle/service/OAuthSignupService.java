package first_project.recycle.service;

import first_project.recycle.domain.User;
import first_project.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthSignupService {

    private final UserMapper userMapper;
    private final EcoPointHistoryService ecoPointHistoryService;

    @Transactional
    public void signup(User user) {

        // 소셜 회원 최종 저장
        userMapper.insertOAuthUser(user);

        // 신규 가입 포인트 지급
        ecoPointHistoryService.earnPoint(
                user.getMemberId(),
                100,
                "SIGNUP",
                user.getMemberId()
        );
    }
}