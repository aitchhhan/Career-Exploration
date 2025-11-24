package career.exploration.dto.Request;

import career.exploration.enums.AllowedFileType;
import career.exploration.enums.FileStatusType;

public record ReqUpdateLectureFile(
        String fileName,
        AllowedFileType fileType,
        Long fileSize,
        String fileUrl,
        String fileKey,
        FileStatusType status
) {}