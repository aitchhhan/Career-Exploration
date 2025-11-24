package career.exploration.repository;


import career.exploration.domain.WeekQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeekQuizRepository extends JpaRepository<WeekQuiz, Long> {
    Optional<WeekQuiz> findReviewWeekById(long reviewWeekId);
}
