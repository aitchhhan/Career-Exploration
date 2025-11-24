package career.exploration.dto;

import lombok.Data;

public class WeekQuizDTO {
    @Data
    public static class showReviewWeek{
        Long ReviewWeekId;
        String title;
        String IsSubmit;
        Integer score;
        Integer total;
    }
}
