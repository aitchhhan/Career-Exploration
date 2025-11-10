package career.exploration.service;



import career.exploration.domain.JoinLectureFile;
import career.exploration.domain.Lecture;
import career.exploration.domain.Member;
import career.exploration.dto.Request.ReqCreateLecture;
import career.exploration.dto.Request.ReqUpdateLecture;
import career.exploration.dto.Response.LectureIncludeFileKeyRes;
import career.exploration.dto.Response.LectureRes;
import career.exploration.enums.FileStatusType;
import career.exploration.exception.InvalidIdException;
import career.exploration.repository.JoinLectureFilesRepository;
import career.exploration.repository.LectureRepository;
import career.exploration.security.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {
    private final LectureRepository lectureRepository;
    private final JoinLectureFilesRepository joinLectureFilesRepository;
    private final JoinLectureFilesService joinLectureFilesService;
    private final S3Service s3Service;
    private final JwtUtility jwtUtility;

    @Transactional // 자료물 생성
    public void createLecture(Member member, ReqCreateLecture req) {
        Lecture lecture = new Lecture(req.title(), req.content(), member.getName());
        Lecture persistedLecture = lectureRepository.save(lecture);
        joinLectureFilesService.createJoinLectureFiles(persistedLecture, req.lectureFileDTOList());
    }

    @Transactional // 자료물 수정
    public void updateLecture(Member member, ReqUpdateLecture req) {
        Lecture lecture = lectureRepository.findById(req.id())
                .orElseThrow(() -> new InvalidIdException("lecture"));

        lecture.update(
                getOrDefault(req.title(), lecture.getTitle()),
                getOrDefault(req.content(), lecture.getContent()),
                member.getName()
        );

        List<ReqUpdateLecture.UpdateLectureFileDTO> files = req.files();
        if (files != null && !files.isEmpty()) {

            // 삭제 대상 파일 삭제
            List<String> keysToDelete = files.stream()
                    .filter(f -> f.status() == FileStatusType.DELETE)
                    .map(ReqUpdateLecture.UpdateLectureFileDTO::fileKey)
                    .toList();
            s3Service.deleteFiles(keysToDelete);
            joinLectureFilesService.deleteFilesByKeyList(lecture, keysToDelete);

            // 새로 추가할 파일만 저장
            List<ReqUpdateLecture.UpdateLectureFileDTO> newFiles = files.stream()
                    .filter(f -> f.status() == FileStatusType.NEW)
                    .toList();
            joinLectureFilesService.updateJoinLectureFiles(lecture, newFiles);
        }
    }

    private <T> T getOrDefault(T newOne, T previousOne) {
        return newOne != null ? newOne : previousOne;
    }

    @Transactional // 자료물 삭제
    public void deleteLecture(Long id) {
        Lecture lecture = lectureRepository.findById(id)
                .orElseThrow(() -> new InvalidIdException("lecture"));

        // 연관된 S3 파일 키 목록 조회
        List<String> fileKeys = joinLectureFilesRepository.findByLecture(lecture).stream()
                .map(JoinLectureFile::getFileKey)
                .filter(key -> key != null && !key.isBlank())
                .toList();

        // S3에서 파일 삭제
        if (!fileKeys.isEmpty()) {
            s3Service.deleteFiles(fileKeys);
        }

        lectureRepository.delete(lecture);
    }

    // 자료물 조회 로직
    public LectureRes findLectureById(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new InvalidIdException("lecture"));

        List<LectureRes.LectureFileDTOWithoutFileKey> fileDTOs = joinLectureFilesRepository.findByLecture(lecture).stream()
                .map(file -> new LectureRes.LectureFileDTOWithoutFileKey(
                        file.getFileName(),
                        file.getFileType(),
                        file.getFileSize(),
                        file.getFileUrl(),
                        file.getFileKey()))
                .toList();

        return new LectureRes(
                lecture.getId(),
                lecture.getTitle(),
                lecture.getContent(),
                lecture.getWriter(),
                lecture.getCreateDateTime(),
                fileDTOs);
    }

    public List<LectureIncludeFileKeyRes> findAllLecture() {
        List<Lecture> lectures = lectureRepository.findAll();
        return lectures.stream()
                .map(this::convertToResponseLectureWithoutFilesDTO)
                .toList();
    }

    private LectureIncludeFileKeyRes convertToResponseLectureWithoutFilesDTO(Lecture lecture) {
        List<JoinLectureFile> files = joinLectureFilesRepository.findByLecture(lecture);
        String fileKey = files.isEmpty() ? null : files.getFirst().getFileKey();

        return new LectureIncludeFileKeyRes(
                lecture.getId(),
                lecture.getTitle(),
                lecture.getContent(),
                lecture.getWriter(),
                lecture.getCreateDateTime(),
                fileKey
        );
    }
}
