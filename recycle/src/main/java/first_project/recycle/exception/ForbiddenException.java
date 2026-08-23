package first_project.recycle.exception;

/**
 * 권한이 없는 요청에서 사용하는 예외
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
