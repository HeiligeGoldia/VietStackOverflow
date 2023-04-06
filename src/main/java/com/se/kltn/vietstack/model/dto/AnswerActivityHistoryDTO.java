package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.answer.AnswerActivityHistory;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class AnswerActivityHistoryDTO {

    private User user;
    private AnswerActivityHistory answerActivityHistory;

}
