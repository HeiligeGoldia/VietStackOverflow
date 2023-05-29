package com.se.kltn.vietstack.model.comment;

import lombok.Data;

import java.util.Date;

@Data
public class AnswerCommentReport {

    private String rcaid;
    private String caid;
    private String uid;
    private String detail;
    private Date date;
    private String status;

}
