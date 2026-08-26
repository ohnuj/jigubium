package first_project.recycle.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 프로젝트 전체에서 발생하는 예외를 공통 처리하는 클래스
 *
 * Controller / Service 등에서 발생한 예외를 받아
 * 상황에 맞는 HTTP 상태 코드와 에러 페이지를 반환한다.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 400 Bad Request
     *
     * 잘못된 값이 전달되었거나
     * 비즈니스 규칙에 맞지 않는 요청
     *
     * 예)
     * - 이미지 개수 초과
     * - 지원하지 않는 파일 형식
     * - 잘못된 포인트 입력
     * - 유효하지 않은 요청 값
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(
            IllegalArgumentException e,
            Model model) {

        log.warn("잘못된 요청: {}", e.getMessage());

        model.addAttribute(
                "message",
                e.getMessage()
        );

        return "error/400";
    }


    /**
     * 400 Bad Request
     *
     * 요청 파라미터의 자료형이 맞지 않는 경우
     *
     * 예)
     * /boards/abc
     *
     * boardId가 Long이어야 하는데 문자열이 들어온 경우
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            Model model) {

        log.warn("요청 파라미터 타입 오류: {}", e.getMessage());

        model.addAttribute(
                "message",
                "올바르지 않은 요청 값입니다."
        );

        return "error/400";
    }


    /**
     * 400 Bad Request
     *
     * 필수 요청 파라미터가 없는 경우
     *
     * 예)
     * ?keyword= 가 반드시 필요한데
     * 파라미터 자체가 전달되지 않은 경우
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            Model model) {

        log.warn("필수 요청 파라미터 누락: {}", e.getMessage());

        model.addAttribute(
                "message",
                "필수 요청 정보가 누락되었습니다."
        );

        return "error/400";
    }


    /**
     * 400 Bad Request
     *
     * 업로드 파일 또는 전체 업로드 용량 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            Model model) {

        log.warn("파일 업로드 용량 초과");

        model.addAttribute(
                "message",
                "업로드 가능한 파일 용량을 초과했습니다."
        );

        return "error/400";
    }


    /**
     * 403 Forbidden
     *
     * 로그인은 되어 있지만
     * 해당 작업을 수행할 권한이 없는 경우
     *
     * 예)
     * - 일반 회원의 관리자 페이지 접근
     * - 다른 회원의 게시글 수정
     * - 다른 회원의 게시글 삭제
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbiddenException(
            ForbiddenException e,
            Model model) {

        log.warn("접근 권한 없음: {}", e.getMessage());

        model.addAttribute(
                "message",
                e.getMessage()
        );

        return "error/403";
    }


    /**
     * 404 Not Found
     *
     * 요청한 데이터가 존재하지 않는 경우
     *
     * 게시글뿐만 아니라
     * 회원 / 리워드 / 재활용 품목 등 모든 데이터에 사용 가능
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(
            NotFoundException e,
            Model model) {

        log.warn("데이터를 찾을 수 없음: {}", e.getMessage());

        model.addAttribute(
                "message",
                e.getMessage()
        );

        return "error/404";
    }


    /**
     * 404 Not Found
     *
     * 존재하지 않는 URL 또는 정적 리소스 접근
     *
     * 예)
     * /abcdefg
     * /css/not-exist.css
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(
            NoResourceFoundException e,
            Model model) {

        log.warn("존재하지 않는 경로 요청: {}", e.getResourcePath());

        model.addAttribute(
                "message",
                "요청한 페이지를 찾을 수 없습니다."
        );

        return "error/404";
    }


    /**
     * 500 Internal Server Error
     *
     * 위에서 별도로 처리하지 않은
     * 예상하지 못한 서버 내부 오류
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(
            Exception e,
            Model model) {

        /*
         * 500 오류는 원인을 확인해야 하므로
         * Stack Trace 전체를 서버 로그에 기록
         */
        log.error("서버 내부 오류 발생", e);

        /*
         * 사용자에게 실제 Exception 메시지는 보여주지 않는다.
         *
         * DB 정보, 클래스명, SQL 등의 내부 정보가
         * 노출될 가능성이 있기 때문
         */
        model.addAttribute(
                "message",
                "요청을 처리하는 중 오류가 발생했습니다."
        );

        return "error/500";
    }
}