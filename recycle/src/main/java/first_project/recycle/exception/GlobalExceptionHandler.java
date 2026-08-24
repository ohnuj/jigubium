package first_project.recycle.exception;


import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    /**
     * 잘못된 요청 -> HTTP 400
     * 이미지 개수, 크기, 형식 등의 검증 실패 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(
            IllegalArgumentException e,
            Model model) {

        model.addAttribute(
                "message",
                e.getMessage()
        );

        return "error/400";
    }

    /**
     * 업로드 전체 용량 또는 파일 용량 초과 -> HTTP 400
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            Model model) {

        model.addAttribute(
                "message",
                "업로드 가능한 파일 용량을 초과했습니다."
        );

        return "error/400";
    }


}
