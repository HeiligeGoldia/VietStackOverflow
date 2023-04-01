package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.answer.AnswerDetail;
import lombok.Data;

import java.util.List;

@Data
public class AnswerAnswerDetailDTO {

    private Answer answer;
    private List<AnswerDetail> answerDetails;

}
