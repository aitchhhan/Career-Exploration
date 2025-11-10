package career.exploration.domain;

import career.exploration.enums.AllowedFileType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity // 강의 자료
public class JoinLectureFile {
    @Id @GeneratedValue
    private Long id; // pk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lecture lecture; // 해당 강의 안내물

    private String fileName; // 강의 자료 이름

    @Enumerated(EnumType.STRING)
    private AllowedFileType fileType; // 강의 자료 타입

    private Long fileSize; // 강의 자료 사이즈

    private String fileUrl; // 강의 자료 // CDN URL

    private String fileKey; // 강의 자료 저장된 경로

    private LocalDateTime uploadDate; // YYYY-MM-DD HH:MM:SS.nnnnnn // 강의 자료 등록일

    private Boolean isUpdate; // true or false // 강의 자료 수정 여부

    // 생성자
    public JoinLectureFile(Lecture lecture, String fileName, AllowedFileType fileType, Long fileSize, String fileUrl, String fileKey, boolean isUpdate) {
        this.lecture = lecture;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileUrl = fileUrl;
        this.fileKey = fileKey;
        this.uploadDate = LocalDateTime.now();
        this.isUpdate = isUpdate;
    }

//    public void updateJoinLectureFile(Lecture lecture, String fileName, AllowedFileType fileType, Long fileSize, String fileUrl, String fileKey) {
//        this.lecture = lecture;
//        this.fileName = fileName;
//        this.fileType = fileType;
//        this.fileSize = fileSize;
//        this.fileUrl = fileUrl;
//        this.fileKey = fileKey;
//        this.isUpdate = isUpdate;
//    }
}
