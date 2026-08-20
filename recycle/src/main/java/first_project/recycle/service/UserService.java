package first_project.recycle.service;

import first_project.recycle.domain.User;
import first_project.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 스프링 빈(Service)으로 등록하여 비즈니스 로직 처리 계층임을 명시
@Service
// final 필드(userMapper)를 매개변수로 받는 생성자를 자동 생성하여 의존성 주입(DI) 처리
@RequiredArgsConstructor
// 클래스 내 모든 메서드에 기본적으로 읽기 전용 트랜잭션 적용 (성능 최적화 및 쓰기 방지)
@Transactional(readOnly = true)
public class UserService {

    // DB 접근을 위한 매퍼 인터페이스 주입
    private final UserMapper userMapper;

    private final EcoPointHistoryService ecoPointHistoryService;

    // 회원가입 처리 (DB 쓰기 작업이므로 readOnly=false인 일반 트랜잭션 적용)
    @Transactional
    public boolean signup(User user) {
        // 1. 동일한 이메일로 가입된 회원이 있는지 중복 검증
        if (userMapper.countByEmail(user.getEmail()) > 0) {
            return false; // 중복된 이메일이 존재하면 가입 실패 반환
        }
        // 2. 중복이 없으면 DB에 회원 정보 저장 (PK 자동 생성 및 user 객체에 세팅)
        userMapper.insert(user);
        // 회원가입시 에코포인트 100p 제공
        ecoPointHistoryService.earnPoint(user.getMemberId(),100,"SIGNUP",user.getMemberId());
        return true; // 가입 성공 반환
    }

    // 로그인 검증 처리 (클래스 레벨의 readOnly=true 트랜잭션 적용)
    public User login(String email, String password) {
        // 1. 입력받은 이메일로 DB에서 회원 단건 조회
        User findUser = userMapper.findByEmail(email);

        // 2. 회원이 존재하지 않거나, 입력한 비밀번호가 일치하지 않는 경우
        if (findUser == null || !findUser.getPassword().equals(password)) {
            return null; // 인증 실패 시 null 반환
        }
        // 3. 인증 성공 시 조회된 회원 정보 객체 반환
        return findUser;
    }
}