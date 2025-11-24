package career.exploration.repository;


import career.exploration.domain.JoinQuizFile;
import career.exploration.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinQuizFileRepository extends JpaRepository<JoinQuizFile, Long> {
    List<JoinQuizFile> findByQuiz(Quiz quiz);
}
