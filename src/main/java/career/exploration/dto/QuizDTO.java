package career.exploration.dto;


import career.exploration.enums.QuizType;
import career.exploration.enums.UpdateQuizStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class QuizDTO {

    @Data
    public static class AddQuizRequest {
        private String title;
        private List<reviewQuizDTO> reviewQuizDTOList;
    }

    @Data
    public static class UpdateQuizRequest {
        private String title;
        private List<ShowReviewQuizDetails> showReviewQuizDTOList;
    }

    @Data
    public static class reviewQuizDTO {
        private QuizType quizType;
        private String content;
        private List<String> answerChoiceList;
        private String answer;
        private List<JoinQuizFileDTO.JoinReviewQuizFileField> files;
        private String explanation;
    }

    @Data
    public static class ShowReviewQuizDetails {
        private Long id;
        private QuizType quizType;
        private String content;
        private List<String> answerChoiceList;
        private List<JoinQuizFileDTO.JoinReviewQuizFileField> files;
        private String answer;
        private String explanation;
        private String response;
    }


    @Data
    public static class SolveAnswerList {
        List<QuizAnswerList> quizAnswerList;
        Integer score;  //맞은 개수
        Integer total; //총 개수
    }

    @Data
    public static class QuizAnswerList{
        Long quizId;
        String answer;
        String explanation;
    }

    @Data
    public static class SolveRequest{
        Long reviewWeekId;
        List<QuizResponse> quizResponseList;
    }

    @Data
    public static class QuizResponse{
        Long quizId;
        String quizAnswer;

    }

    @Data
    public static class reviewQuizEditDTO {
        private Long reviewQuizId;

        private UpdateQuizStatus status;

        private QuizType quizType;

        private String content;

        private List<String> answerChoiceList;

        private String answer;

        private List<JoinQuizFileDTO.JoinReviewQuizFileField> files;

        private String explanation;
    }

    @Data
    public static class EditQuizRequest {
        private String title;

        private List<reviewQuizEditDTO> reviewQuizDTOList;
    }

    @Data
    public static class GetQuiz {
        private String title;
        private List<QuizList> quizList;
    }

    @Data
    public static class QuizList {
        private String name;
        private int count;
        private Integer score;  //맞은 개수
        private Integer total; //총 개수
        private LocalDateTime updateDate;
    }

}
