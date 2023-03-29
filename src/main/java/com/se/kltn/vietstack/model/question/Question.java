package com.se.kltn.vietstack.model.question;

import lombok.Data;

import java.util.Date;

@Data
public class Question {

    private String qid;
    private String uid;
    private Date date;
    private String status;

}