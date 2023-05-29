package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.comment.AnswerComment;
import com.se.kltn.vietstack.model.comment.AnswerCommentReport;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class AnswerCommentReportDTO {

    private AnswerCommentReport answerCommentReport;
    private Question question;
    private Answer answer;
    private AnswerComment answerComment;
    private User user;

}
