package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.answer.AnswerDetail;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

import java.util.List;

@Data
public class AnswerDTO {

    private Answer answer;
    private List<AnswerDetail> answerDetails;
    private int answerVote;
    private User user;
    private String voteValue;

}
