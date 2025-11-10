package career.exploration.dto.Response;

import java.time.LocalDateTime;

public record LectureIncludeFileKeyRes(
        Long id,
        String title,
        String content,
        String writer,
        LocalDateTime createDateTime,
        String fileKey
) {
}
