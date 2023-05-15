package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.tag.Tag;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {

    private Question question;
    private List<Tag> tags;
    private int questionVote;
    private int answerCount;
    private User user;
    private boolean acceptAnswerAvailable;

}
