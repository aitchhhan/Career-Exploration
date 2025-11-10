package career.exploration.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity // 자료
public class Lecture {
    @Id @GeneratedValue
    private Long id; // pk가

    private String title; // 강의 안내물 제목

    @Lob
    private String content; // 강의 안내물 내용

    private String writer; // 강의 안내물 작성자

    private LocalDateTime createDateTime; // YYYY-MM-DD HH:MM:SS.nnnnnn // 강의 안내물 생성일

    private LocalDateTime updateDateTime; // YYYY-MM-DD HH:MM:SS.nnnnnn // 강의 안내물 수정일

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JoinLectureFile> files = new ArrayList<>();

    // 생성자
    public Lecture(String title, String content, String writer) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createDateTime = LocalDateTime.now(); // 생성 당시 시간
        this.updateDateTime = null;
    }

    // 업데이트
    public void update(String title, String content, String writer) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.updateDateTime = LocalDateTime.now(); // 수정 당시 시간
    }
}
