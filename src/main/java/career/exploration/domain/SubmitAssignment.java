package career.exploration.domain;

import career.exploration.enums.PassStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAssignment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member lion;

    private String content;

    @Enumerated(EnumType.STRING)
    private PassStatus passNonePass;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}
