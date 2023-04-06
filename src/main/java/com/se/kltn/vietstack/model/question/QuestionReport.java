package com.se.kltn.vietstack.model.question;

import lombok.Data;

import java.util.Date;

@Data
public class QuestionReport {

    private String rqid;
    private String qid;
    private String uid;
    private String detail;
    private Date date;
    private String status;

}
