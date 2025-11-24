package career.exploration.domain;

import career.exploration.enums.QuizType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
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

    public Quiz(WeekQuiz weekQuiz, String content, String explanation, String answer, QuizType quizType) {
        this.weekQuiz = weekQuiz;
        this.content = content;
        this.explanation = explanation;
        this.answer = answer;
        this.quizType = quizType;
    }
}

