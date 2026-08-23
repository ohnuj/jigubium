package first_project.recycle.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 프로젝트 전역 예외 처리
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 권한이 없는 요청 -> HTTP 403
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handelForbiddenException(
            ForbiddenException e) {

        return "error/403";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handelNotFoundException(
            NotFoundException e) {

        return "error/404";
    }


}
