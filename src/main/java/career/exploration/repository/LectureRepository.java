package career.exploration.repository;



import career.exploration.domain.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    // id로 강의 안내물 반환 (강의자료를 제외한)
    Optional<Lecture> findById(Long id);

    // 전체 강의 안내물 리스트 반환 (강의자료를 제외한)
    List<Lecture> findAll();

}
