package career.exploration.domain;

import career.exploration.enums.AnswerStatus;
import career.exploration.enums.QuizType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class QuizResponse {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Quiz quiz;

    private String answer;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    @Enumerated(EnumType.STRING)
    private AnswerStatus answerStatus;

    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    private int count;

    public QuizResponse(Member member, Quiz quiz, String userAnswer, QuizType quizType) {
        this.member = member;
        this.quiz = quiz;
        this.answer = userAnswer;
        this.quizType = quizType;
    }
}

