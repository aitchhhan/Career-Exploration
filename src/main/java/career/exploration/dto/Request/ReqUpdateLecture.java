package career.exploration.dto.Request;

import career.exploration.enums.AllowedFileType;
import career.exploration.enums.FileStatusType;

import java.util.List;

public record ReqUpdateLecture(
        Long id,
        String title,
        String content,
        List<UpdateLectureFileDTO> files
) {
    public record UpdateLectureFileDTO(
            String fileName,
            AllowedFileType fileType,
            Long fileSize,
            String fileUrl,
            String fileKey,
            FileStatusType status
    ) {}
}