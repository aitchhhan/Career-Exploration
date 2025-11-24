package career.exploration.service.reviewquiz;


import career.exploration.domain.*;
import career.exploration.dto.JoinQuizFileDTO;
import career.exploration.dto.QuizDTO;
import career.exploration.dto.WeekQuizDTO;
import career.exploration.enums.AnswerStatus;
import career.exploration.enums.QuizType;
import career.exploration.exception.InvalidIdException;
import career.exploration.repository.*;
import career.exploration.service.GeminiService;
import career.exploration.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuizService {

    private final WeekQuizRepository weekQuizRepository;
    private final QuizRepository quizRepository;
    private final JoinQuizFileService joinQuizFileService;
    private final AnswerChoiceRepository answerChoiceRepository;
    private final QuizResponseRepository quizResponseRepository;
    private final GeminiService geminiService;
    private final JoinQuizFileRepository joinQuizFileRepository;
    private final S3Service s3Service;

    @Transactional
    public void addQuiz(QuizDTO.AddQuizRequest req) {
        WeekQuiz reviewWeek = new WeekQuiz(req.getTitle());
        weekQuizRepository.save(reviewWeek);
        for (QuizDTO.reviewQuizDTO reviewQuizDTO : req.getReviewQuizDTOList()) {
            Quiz quiz = new Quiz(
                    reviewWeek,
                    reviewQuizDTO.getContent(),
                    reviewQuizDTO.getExplanation(),
                    reviewQuizDTO.getAnswer(),
                    reviewQuizDTO.getQuizType());
            quizRepository.save(quiz);

            if (reviewQuizDTO.getFiles()!= null) {
                joinQuizFileService.createJoinReviewQuizFiles(quiz, reviewQuizDTO.getFiles());
            }

            if (reviewQuizDTO.getQuizType()== QuizType.MULTIPLE_CHOICE){
                for(String StringAnswerChoice : reviewQuizDTO.getAnswerChoiceList()){
                    AnswerChoice answerChoice = new AnswerChoice(quiz, StringAnswerChoice);
                    answerChoiceRepository.save(answerChoice);
                }
            }
        }
    }

    //이미 푼 퀴즈 새로 응답 저장 or 응답 업데이트
    private QuizResponse getOrCreateResponse(Member member, Quiz quiz, String userAnswer) {
        QuizResponse existingResponse = quizResponseRepository.findQuizResponseByMemberAndQuiz(member, quiz);

        if (existingResponse == null) {
            return new QuizResponse(member, quiz, userAnswer, quiz.getQuizType());
        } else {
            existingResponse.setUpdateDate(LocalDateTime.now());
            existingResponse.setCount(existingResponse.getCount() + 1);
            existingResponse.setAnswer(userAnswer);
            return existingResponse;
        }
    }

    //객관식 채점
    private boolean gradeMultipleChoice(Quiz quiz, QuizResponse response, String answer) {
        if (Objects.equals(quiz.getAnswer(), answer)) {
            response.setAnswerStatus(AnswerStatus.TRUE);
            return true;
        } else {
            response.setAnswerStatus(AnswerStatus.FALSE);
            return false;
        }
    }

    /**
     * 주관식 문제를 Gemini API사용해서 평가하기.
     * @param quiz 평가할 문제
     * @param userAnswer 사용자 답변
     */
    private AnswerStatus evaluateEssayQuestion(Quiz quiz, String userAnswer) {
        try {
            // 문제의 내용(content)과 정답(answer)을 모두 Gemini에 전달
            boolean isCorrect = geminiService.evaluateEssayAnswer(quiz.getContent(), quiz.getAnswer(), userAnswer);
            if (isCorrect) {
                log.info("AI evaluated answer as correct for quiz ID: {}", quiz.getId());
                return AnswerStatus.TRUE;
            } else {
                log.info("AI evaluated answer as incorrect for quiz ID: {}", quiz.getId());
                return AnswerStatus.FALSE;
            }
        } catch (Exception e) {
            log.error("Error evaluating essay answer with Gemini API", e);
            return AnswerStatus.EMPTY; // API 오류 시 기본값으로 설정
        }
    }

    //정답/해설 리스트 생성
    private QuizDTO.QuizAnswerList createQuizAnswerList(Quiz quiz) {
        QuizDTO.QuizAnswerList quizAnswerList = new QuizDTO.QuizAnswerList();
        quizAnswerList.setQuizId(quiz.getId());
        quizAnswerList.setAnswer(quiz.getAnswer());
        quizAnswerList.setExplanation(quiz.getExplanation());
        return quizAnswerList;
    }

    //정답 여부 반환
    private boolean evaluateAndMarkAnswer(Quiz quiz, QuizResponse response, String userAnswer) {
        if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
            return gradeMultipleChoice(quiz, response, userAnswer); // 내부에서 answerStatus 설정까지 함
        } else if (quiz.getQuizType() == QuizType.ESSAY_QUESTION) {
            AnswerStatus status = evaluateEssayQuestion(quiz, userAnswer);
            response.setAnswerStatus(status);
            return status == AnswerStatus.TRUE;
        } else {
            response.setAnswerStatus(AnswerStatus.EMPTY);
            return false;
        }
    }

    //답 채점 및 저장
    @Transactional
    public QuizDTO.SolveAnswerList solveQuiz(Member member, QuizDTO.SolveRequest solveRequest) {
        WeekQuiz reviewWeek = weekQuizRepository.findReviewWeekById(solveRequest.getReviewWeekId())
                .orElseThrow(()-> new InvalidIdException("weekId에 대한 WeekQuiz 없음"));

        List<Quiz> reviewQuizzes = quizRepository.findByWeekQuiz(reviewWeek);
        List<QuizDTO.QuizResponse> userAnswers = solveRequest.getQuizResponseList();

        QuizDTO.SolveAnswerList solveAnswerList = new QuizDTO.SolveAnswerList();
        solveAnswerList.setQuizAnswerList(new ArrayList<>());

        int correctCount = 0;
        int total = reviewQuizzes.size();

        for (int i = 0; i < reviewQuizzes.size(); i++) {
            Quiz quiz = reviewQuizzes.get(i);
            QuizDTO.QuizResponse memberAnswerDTO = userAnswers.get(i);
            QuizResponse response = getOrCreateResponse(member, quiz, memberAnswerDTO.getQuizAnswer());

            boolean isCorrect = evaluateAndMarkAnswer(quiz, response, memberAnswerDTO.getQuizAnswer());
            if (isCorrect) correctCount++;


            if (response.getId() == null) {
                quizResponseRepository.save(response);
            }

            solveAnswerList.getQuizAnswerList().add(createQuizAnswerList(quiz));
        }

        solveAnswerList.setScore(correctCount);
        solveAnswerList.setTotal(total);
        return solveAnswerList;
    }

    //주차별 퀴즈 목록 조회
    public List<WeekQuizDTO.showReviewWeek> getReviewWeek(Member member) {
        List<WeekQuiz> reviewWeekList = weekQuizRepository.findAll();
        System.out.println(reviewWeekList);

        List<WeekQuizDTO.showReviewWeek> reviewWeekDTOList = new ArrayList<>();

        for (WeekQuiz reviewWeek : reviewWeekList) {
            String IsSubmit="제출";
            int correctCount = 0;
            WeekQuizDTO.showReviewWeek dto = new WeekQuizDTO.showReviewWeek();
            dto.setReviewWeekId(reviewWeek.getId());
            dto.setTitle(reviewWeek.getTitle());
            List<Quiz> reviewQuizzes = quizRepository.findByWeekQuiz(reviewWeek);
            for(Quiz reviewQuiz : reviewQuizzes) {
                QuizResponse existingResponse = quizResponseRepository.findQuizResponseByMemberAndQuiz(member, reviewQuiz);
                if (existingResponse == null) {
                    IsSubmit = "미제출";
                    break;
                }
                if(existingResponse.getAnswerStatus()==AnswerStatus.TRUE) {
                    correctCount++;
                }
            }
            dto.setScore(correctCount);
            dto.setTotal(reviewQuizzes.size());
            dto.setIsSubmit(IsSubmit);
            reviewWeekDTOList.add(dto);
        }
        return reviewWeekDTOList;
    }

    //복습 퀴즈 상세 조회
    public List<QuizDTO.ShowReviewQuizDetails> getReviewQuiz(Long WeekId, Member member) {
        WeekQuiz reviewWeek = weekQuizRepository.findReviewWeekById(WeekId).
                orElseThrow(()-> new InvalidIdException("해당 주차에 대한 복습퀴즈를 찾을 수 없습니다."));
        List<Quiz> reviewQuizzes =  quizRepository.findByWeekQuiz(reviewWeek);

        return reviewQuizzes.stream()
                .map(reviewQuiz -> {
                    QuizDTO.ShowReviewQuizDetails dto = new QuizDTO.ShowReviewQuizDetails();
                    dto.setId(reviewQuiz.getId()); //문제 아이디
                    dto.setQuizType(reviewQuiz.getQuizType());//문제 타입
                    dto.setContent(reviewQuiz.getContent());//문제 내용
                    // 객관식일 때만 보기 리스트 설정
                    if (reviewQuiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                        List<String> answerChoices = answerChoiceRepository.findByQuiz(reviewQuiz).stream()
                                .map(AnswerChoice::getContent)
                                .collect(Collectors.toList());
                        dto.setAnswerChoiceList(answerChoices);
                    } else {
                        dto.setAnswerChoiceList(Collections.emptyList());
                    }
                    // 첨부 파일 DTO 매핑
                    List<JoinQuizFileDTO.JoinReviewQuizFileField> fileDtos =
                            joinQuizFileRepository.findByQuiz(reviewQuiz).stream()
                                    .map(file -> {
                                        JoinQuizFileDTO.JoinReviewQuizFileField fileDto =
                                                new JoinQuizFileDTO.JoinReviewQuizFileField();
                                        fileDto.setFileName(file.getFileName());
                                        fileDto.setFileType(file.getFileType());
                                        fileDto.setFileSize(file.getFileSize());
                                        fileDto.setFileUrl(file.getFileUrl());
                                        fileDto.setFileKey(file.getFileKey());
                                        return fileDto;
                                    })
                                    .collect(Collectors.toList());

                    QuizResponse response = quizResponseRepository.findQuizResponseByMemberAndQuiz(member, reviewQuiz);
                    if (response != null) {
                        dto.setResponse(response.getAnswer());
                    } else {
                        dto.setResponse(null);
                    }

                    dto.setFiles(fileDtos);
                    dto.setAnswer(reviewQuiz.getAnswer());
                    dto.setExplanation(reviewQuiz.getExplanation());
                    return dto;
                }).toList();
    }


    @Transactional
    public void updateQuizByStatus(Long weekId, QuizDTO.EditQuizRequest request) {
        WeekQuiz reviewWeek = weekQuizRepository.findReviewWeekById(weekId)
                .orElseThrow(() -> new InvalidIdException("해당 주차에 대한 퀴즈를 찾을 수 없습니다."));

        // 주차 제목, 트랙 정보 업데이트
        reviewWeek.update(request.getTitle());
        weekQuizRepository.save(reviewWeek);

        for (QuizDTO.reviewQuizEditDTO dto : request.getReviewQuizDTOList()) {
            switch (dto.getStatus()) {
                case UPDATE -> handleUpdate(dto);
                case DELETE -> handleDelete(dto);
                case CREATE -> handleCreate(dto, reviewWeek);
                case KEEP -> {
                    // 아무것도 하지 않음 (유지)
                }
                default -> throw new IllegalArgumentException("잘못된 상태입니다: " + dto.getStatus());
            }
        }
    }

    private void handleCreate(QuizDTO.reviewQuizEditDTO dto, WeekQuiz weekQuiz) {
        Quiz newQuiz = new Quiz(
                weekQuiz,
                dto.getContent(),
                dto.getExplanation(),
                dto.getAnswer(),
                dto.getQuizType()
        );
        quizRepository.save(newQuiz);

        if (dto.getQuizType() == QuizType.MULTIPLE_CHOICE && dto.getAnswerChoiceList() != null) {
            for (String choice : dto.getAnswerChoiceList()) {
                answerChoiceRepository.save(new AnswerChoice(newQuiz, choice));
            }
        }

        if (dto.getFiles() != null) {
            List<JoinQuizFile> files = dto.getFiles().stream()
                    .map(fileDTO -> new JoinQuizFile(
                            newQuiz,
                            fileDTO.getFileName(),
                            fileDTO.getFileType(),
                            fileDTO.getFileSize(),
                            fileDTO.getFileUrl(),
                            fileDTO.getFileKey()
                    )).toList();
            joinQuizFileRepository.saveAll(files);
        }
    }

    private void handleUpdate(QuizDTO.reviewQuizEditDTO dto) {
        Quiz quiz = quizRepository.findById(dto.getReviewQuizId())
                .orElseThrow(() -> new InvalidIdException("수정할 퀴즈가 존재하지 않습니다."));

        quiz.setQuizType(dto.getQuizType());
        quiz.setContent(dto.getContent());
        quiz.setAnswer(dto.getAnswer());
        quiz.setExplanation(dto.getExplanation());

        // 기존 응답 삭제
        List<QuizResponse> responses = quizResponseRepository.findByQuiz(quiz);
        quizResponseRepository.deleteAll(responses);

        // 객관식 보기 업데이트
        answerChoiceRepository.deleteByQuiz(quiz);
        if (dto.getQuizType() == QuizType.MULTIPLE_CHOICE && dto.getAnswerChoiceList() != null) {
            for (String content : dto.getAnswerChoiceList()) {
                answerChoiceRepository.save(new AnswerChoice(quiz, content));
            }
        }

        // 기존 파일 삭제
        List<JoinQuizFile> oldFiles = joinQuizFileRepository.findByQuiz(quiz);
        if (!oldFiles.isEmpty()) {
            List<String> fileKeys = oldFiles.stream().map(JoinQuizFile::getFileKey).toList();
            s3Service.deleteFiles(fileKeys);
            joinQuizFileRepository.deleteAll(oldFiles);
        }

        // 새 파일 저장
        if (dto.getFiles() != null) {
            List<JoinQuizFile> newFiles = dto.getFiles().stream()
                    .map(fileDTO -> new JoinQuizFile(
                            quiz,
                            fileDTO.getFileName(),
                            fileDTO.getFileType(),
                            fileDTO.getFileSize(),
                            fileDTO.getFileUrl(),
                            fileDTO.getFileKey()
                    )).toList();
            joinQuizFileRepository.saveAll(newFiles);
        }
    }

    private void handleDelete(QuizDTO.reviewQuizEditDTO dto) {
        Quiz quiz = quizRepository.findById(dto.getReviewQuizId())
                .orElseThrow(() -> new InvalidIdException("삭제할 퀴즈가 존재하지 않습니다."));

        // 보기 삭제
        answerChoiceRepository.deleteByQuiz(quiz);

        // 파일 삭제
        List<JoinQuizFile> files = joinQuizFileRepository.findByQuiz(quiz);
        if (!files.isEmpty()) {
            List<String> fileKeys = files.stream().map(JoinQuizFile::getFileKey).toList();
            s3Service.deleteFiles(fileKeys);
            joinQuizFileRepository.deleteAll(files);
        }

        // 응답 삭제
        List<QuizResponse> responses = quizResponseRepository.findByQuiz(quiz);
        quizResponseRepository.deleteAll(responses);

        // 퀴즈 삭제
        quizRepository.delete(quiz);
    }


    @Transactional
    public void deleteQuiz(Long weekId){
        WeekQuiz reviewWeek = weekQuizRepository.findById(weekId)
                .orElseThrow(()-> new InvalidIdException("해당 주차에 대한 퀴즈를 찾을 수 없습니다."));
        List<Quiz> reviewQuizList = quizRepository.findByWeekQuiz(reviewWeek);
        for (Quiz reviewQuiz : reviewQuizList) {
            //해당 문제에 대한 파일 메타데이터 삭제
            List<JoinQuizFile> joinReviewQuizFileList = joinQuizFileRepository.findByQuiz(reviewQuiz);

            if (!joinReviewQuizFileList.isEmpty()) {
                List<String> keys = joinReviewQuizFileList.stream().map(JoinQuizFile::getFileKey).toList();
                s3Service.deleteFiles(keys);
                joinQuizFileRepository.deleteAll(joinReviewQuizFileList);
            }

        }
        quizRepository.deleteAll(reviewQuizList);
        weekQuizRepository.delete(reviewWeek);
    }

    @Transactional
    public QuizDTO.GetQuiz getQuiz(Long weekId) {
        WeekQuiz reviewWeek = weekQuizRepository.findReviewWeekById(weekId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주차 정보가 없습니다."));

        QuizDTO.GetQuiz getQuiz = new QuizDTO.GetQuiz();
        getQuiz.setTitle(reviewWeek.getTitle());

        List<Quiz> reviewQuizzes = quizRepository.findByWeekQuiz(reviewWeek);
        List<QuizResponse> allResponses = quizResponseRepository.findByQuizIn(reviewQuizzes);

        List<QuizDTO.QuizList> quizLists = new ArrayList<>();
        Set<Long> processedMemberIds = new HashSet<>();

        for (QuizResponse response : allResponses) {
            Member member = response.getMember();
            Long memberId = member.getId();

            if (processedMemberIds.contains(memberId)) {
                continue;
            }
            processedMemberIds.add(memberId);

            // 같은 사용자의 응답 모으기
            List<QuizResponse> memberResponses = new ArrayList<>();
            for (QuizResponse r : allResponses) {
                if (r.getMember().getId().equals(memberId)) {
                    memberResponses.add(r);
                }
            }
            // 통계 계산
            int correct = 0;
            int countSum = 0;
            LocalDateTime latest = null;
            for (QuizResponse r : memberResponses) {
                if (r.getAnswerStatus() == AnswerStatus.TRUE) {
                    correct++;
                }
                countSum = r.getCount();
                if (latest == null || r.getUpdateDate().isAfter(latest)) {
                    latest = r.getUpdateDate();
                }
            }

            // DTO에 담기
            QuizDTO.QuizList dto = new QuizDTO.QuizList();
            dto.setName(member.getName());
            dto.setCount(countSum);
            dto.setScore(correct);
            dto.setTotal(reviewQuizzes.size()); // 전체 문제 수
            dto.setUpdateDate(latest);

            quizLists.add(dto);
        }
        getQuiz.setQuizList(quizLists);
        return getQuiz;
    }
}
