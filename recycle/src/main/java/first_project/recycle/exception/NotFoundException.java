package first_project.recycle.exception;


/**
 * 요청한 데이터가 존재하지 않을 때 사용하는 예외
 */

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}