package career.exploration.exception;

public class S3PresignedException extends RuntimeException{
    public S3PresignedException(String message){
        super(message);
    }
}
