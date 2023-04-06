package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.question.QuestionReport;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class QuestionReportDTO {

    private QuestionReport questionReport;
    private User user;

}
