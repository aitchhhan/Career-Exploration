package career.exploration.repository;


import career.exploration.domain.AnswerChoice;
import career.exploration.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerChoiceRepository extends JpaRepository<AnswerChoice, Integer> {
    List<AnswerChoice> findByQuiz(Quiz reviewQuiz);
    void deleteByQuiz(Quiz reviewQuiz);
}
