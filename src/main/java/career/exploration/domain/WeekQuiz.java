package career.exploration.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class WeekQuiz {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;
}

