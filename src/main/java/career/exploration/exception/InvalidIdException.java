package career.exploration.exception;

public class InvalidIdException extends RuntimeException {
    // 이 id에 해당하는 값 없

    public InvalidIdException(String message) {
        super(message);
    }
}
