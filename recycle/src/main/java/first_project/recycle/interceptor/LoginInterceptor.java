package first_project.recycle.interceptor;

import first_project.recycle.config.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 기존 세션 조회
        // 세션이 없으면 새로 생성X
        HttpSession session = request.getSession(false);

        // 1. 세션 자체가 없는 경우
        if (session == null) {
            response.sendRedirect("/login");
            return false;
        }

        // 2. 세션은 잇지만 로그인 회원 정보가 없는 경우
        Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if(loginMember==null){
            response.sendRedirect("/login");
            return false;
        }

        // 3. 로그인 사용자면 통과
        return true;
    }
}
