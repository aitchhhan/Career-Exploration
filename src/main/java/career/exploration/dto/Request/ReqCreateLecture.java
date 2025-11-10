package career.exploration.dto.Request;

import career.exploration.enums.AllowedFileType;

import java.util.List;

public record ReqCreateLecture(
        String title,
        String content,
        List<LectureFileDTO> lectureFileDTOList
) {
    public record LectureFileDTO(
            String fileName,
            AllowedFileType fileType,
            Long fileSize,
            String fileUrl,
            String fileKey
    ) {}
}