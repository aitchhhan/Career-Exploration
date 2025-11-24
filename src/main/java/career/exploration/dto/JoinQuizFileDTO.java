package career.exploration.dto;


import career.exploration.enums.AllowedFileType;
import lombok.Data;

public class JoinQuizFileDTO {

    @Data
    public static class JoinReviewQuizFileField{
        private String fileName;
        private AllowedFileType fileType;
        private Long fileSize;
        private String fileUrl;
        private String fileKey;
    }
}
