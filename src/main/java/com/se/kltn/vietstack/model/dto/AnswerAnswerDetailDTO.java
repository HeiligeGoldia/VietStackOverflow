package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.answer.AnswerDetail;
import lombok.Data;

@Data
public class AnswerAnswerDetailDTO {

    private Answer answer;
    private AnswerDetail answerDetail;

}
