package career.exploration.domain;

import career.exploration.enums.QuizType;
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
public class Quiz {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private WeekQuiz weekQuiz;

    private String content;

    private String explanation;

    private String answer;

    @Enumerated(EnumType.STRING)
    private QuizType quizType;
}

