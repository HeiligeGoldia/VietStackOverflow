package com.se.kltn.vietstack.model.question;

import lombok.Data;

import java.util.Date;

@Data
public class QuestionActivityHistory {

    private String qahid;
    private String qid;
    private String uid;
    private String action;
    private String description;
    private Date date;

}
