package career.exploration.dto.Response;

import career.exploration.enums.AllowedFileType;

import java.time.LocalDateTime;
import java.util.List;

public record LectureRes(
        Long id,
        String title,
        String content,
        String writer,
        LocalDateTime createDateTime,
        List<LectureFileDTOWithoutFileKey> joinLectureFiles
) {
    public record LectureFileDTOWithoutFileKey(
            String fileName,
            AllowedFileType fileType,
            Long fileSize,
            String fileUrl,
            String fileKey
    ) {}

}

