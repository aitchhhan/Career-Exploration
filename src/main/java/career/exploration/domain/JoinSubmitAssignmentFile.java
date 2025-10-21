package career.exploration.domain;

import career.exploration.enums.AllowedFileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinSubmitAssignmentFile {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SubmitAssignment submitAssignment;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private AllowedFileType fileType;

    private Long fileSize;

    private String fileUrl;

    private String fileKey;
}

