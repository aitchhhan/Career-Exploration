package career.exploration.repository;


import career.exploration.domain.Quiz;
import career.exploration.domain.WeekQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    //@Query("SELECT rq FROM ReviewQuiz rq WHERE rq.reviewWeek.id = :reviewWeekId")
    List<Quiz> findByWeekQuiz(WeekQuiz reviewWeek);

    Optional<Quiz> findById(Long id);
}
