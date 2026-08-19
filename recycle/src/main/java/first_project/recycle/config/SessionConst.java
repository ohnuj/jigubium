package first_project.recycle.config;

// 세션 키 값 관리용 상수 클래스 (객체 생성을 방지하기 위해 인터페이스 또는 추상 클래스로도 활용 가능)
public class SessionConst {

    /*
     * [세션(loginMember)에 저장되는 User 객체 내부 필드 구성]
     * - memberId   : 회원 고유 식별자 (PK)
     * - email      : 로그인 이메일 계정
     * - nickname   : 서비스 내 표시 닉네임
     * - name       : 회원 실명
     * - role       : 회원 권한 (기본값: USER)
     * - provider   : 가입 경로/인증 제공자 (기본값: LOCAL)
     * - providerId : 소셜 로그인 고유 식별값
     * (* 보안을 위해 password 필드는 세션 저장 직전 null로 초기화됨)
     */

    // HttpSession에 로그인 회원 객체를 바인딩할 때 사용하는 세션 속성 키(Key) 상수
    public static final String LOGIN_MEMBER = "loginMember";
}