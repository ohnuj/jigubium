package first_project.recycle.interceptor;

import first_project.recycle.config.SessionConst;
import first_project.recycle.domain.SessionUser;
import first_project.recycle.domain.User;
import first_project.recycle.exception.ForbiddenException;
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

        // 세션이 없거나 로그인 정보가 없으면 차단
        if (session == null) {
            throw new ForbiddenException("관리자만 접근할 수 있습니다.");
        }

        SessionUser loginUser =
                (SessionUser) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (loginUser == null) {
            throw new ForbiddenException("관리자만 접근할 수 있습니다.");
        }

        // ADMIN이 아니면 차단
        if (!"ADMIN".equals(loginUser.getRole())) {
            throw new ForbiddenException("관리자만 접근할 수 있습니다.");
        }

        // ADMIN 통과
        return true;
    }
}

