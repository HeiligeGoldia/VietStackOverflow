package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.answer.AnswerReport;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class AnswerReportDTO {

    private AnswerReport answerReport;
    private User user;

}
