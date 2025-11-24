package career.exploration.repository;


import career.exploration.domain.Member;
import career.exploration.domain.Quiz;
import career.exploration.domain.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {
    QuizResponse findQuizResponseByMemberAndQuiz(Member member, Quiz quiz);
    List<QuizResponse> findByQuiz(Quiz quiz);
    List<QuizResponse> findByQuizIn(List<Quiz> quizList);
}
