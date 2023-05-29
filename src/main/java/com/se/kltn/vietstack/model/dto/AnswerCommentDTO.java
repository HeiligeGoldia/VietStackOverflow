package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.comment.AnswerComment;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class AnswerCommentDTO {

    private AnswerComment answerComment;
    private User user;

}
