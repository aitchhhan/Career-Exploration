package career.exploration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyMemberException.class)
    public ResponseEntity<String> invalidMember(EmptyMemberException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("이런 member 없");
    }

    @ExceptionHandler(InvalidLoginlException.class)
    public ResponseEntity<String> invalidLogin(InvalidLoginlException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(InvalidIdException.class)
    public ResponseEntity<String> invalidId(InvalidIdException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage() + "Id로 조회한 결과 없음");
    }

    // Jwt
    @ExceptionHandler(HandleJwtException.class)
    public ResponseEntity<String> handleJwt(HandleJwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<String> InvalidJwt(InvalidJwtException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    // S3
    @ExceptionHandler(S3PresignedException.class)
    public ResponseEntity<String> s3Presigned(S3PresignedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(NotAllowedFileTypeException.class)
    public ResponseEntity<String> notAllowedFileType(NotAllowedFileTypeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("허용되지 않은 MIME 타입입니다.");
    }
}
