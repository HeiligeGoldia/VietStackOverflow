package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.question.QuestionActivityHistory;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class QuestionActivityHistoryDTO {

    private User user;
    private QuestionActivityHistory questionActivityHistory;

}
