package career.exploration.service;



import career.exploration.domain.JoinLectureFile;
import career.exploration.domain.Lecture;
import career.exploration.dto.Request.ReqCreateLecture;
import career.exploration.dto.Request.ReqUpdateLecture;
import career.exploration.dto.Request.ReqUpdateLectureFile;
import career.exploration.repository.JoinLectureFilesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinLectureFilesService {
    private final JoinLectureFilesRepository joinLectureFilesRepository;

    @Transactional
    public void createJoinLectureFiles(Lecture lecture, List<ReqCreateLecture.LectureFileDTO> files) {
        List<JoinLectureFile> joinLectureFileList = files.stream()
                .map(dto -> new JoinLectureFile(
                        lecture,
                        dto.fileName(),
                        dto.fileType(),
                        dto.fileSize(),
                        dto.fileUrl(),
                        dto.fileKey(),
                        false
                ))
                .toList();

        joinLectureFilesRepository.saveAll(joinLectureFileList);
    }

    @Transactional
    public void updateJoinLectureFiles(Lecture lecture, List<ReqUpdateLecture.UpdateLectureFileDTO> files) {
        List<JoinLectureFile> newJoinLectureFileList = files.stream()
                .map(dto -> new JoinLectureFile(
                        lecture,
                        dto.fileName(),
                        dto.fileType(),
                        dto.fileSize(),
                        dto.fileUrl(),
                        dto.fileKey(),
                        true
                ))
                .toList();

        joinLectureFilesRepository.saveAll(newJoinLectureFileList);
    }

    public void deleteFilesByKeyList(Lecture lecture, List<String> keysToDelete) {
        if (keysToDelete == null || keysToDelete.isEmpty()) return;
        joinLectureFilesRepository.deleteAllByLectureAndFileKeyIn(lecture, keysToDelete);
    }
}
