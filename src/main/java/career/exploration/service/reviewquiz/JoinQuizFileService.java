package career.exploration.service.reviewquiz;


import career.exploration.domain.JoinQuizFile;
import career.exploration.domain.Quiz;
import career.exploration.dto.JoinQuizFileDTO;
import career.exploration.repository.JoinQuizFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinQuizFileService {

    private final JoinQuizFileRepository joinQuizFileRepository;

    @Transactional
    public void createJoinReviewQuizFiles(Quiz reviewQuiz, List<JoinQuizFileDTO.JoinReviewQuizFileField> files) {
        List<JoinQuizFile> joinQuizFileList = files.stream()
                .map(dto -> new JoinQuizFile(
                        reviewQuiz,
                        dto.getFileName(),
                        dto.getFileType(),
                        dto.getFileSize(),
                        dto.getFileUrl(),
                        dto.getFileKey()
                ))
                .toList();

        joinQuizFileRepository.saveAll(joinQuizFileList);
    }
}
