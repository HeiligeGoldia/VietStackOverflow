package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.comment.Comment;
import com.se.kltn.vietstack.model.comment.CommentReport;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class CommentReportDTO {

    private CommentReport commentReport;
    private Question question;
    private Comment comment;
    private User user;

}
