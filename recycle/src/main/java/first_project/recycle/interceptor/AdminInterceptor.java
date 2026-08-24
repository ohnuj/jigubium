package first_project.recycle.interceptor;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        //기존세션 가져오기
        HttpSession session = request.getSession(false);

        // 1. 로그인 하지 않은 사용자
        if(session==null){
            response.sendRedirect("/login");
            return false;
        }

        // 2. 세션에서 로그인 사용자 가져오기
        User loginUser = (User)session.getAttribute(SessionConst.LOGIN_MEMBER);

        // 3. 세션은 있지만 로그인 정보가 없는 경우
        if(loginUser==null){
            response.sendRedirect("/login");
            return false;
        }

        // 4. 로그인햇지만 admin이 아닌경우
        if(!"ADMIN".equals(loginUser.getRole())){
            response.sendRedirect("/");
            return false;
        }
        // 5. admin 통과
        return true;
    }
}
