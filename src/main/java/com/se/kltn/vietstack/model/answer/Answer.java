package com.se.kltn.vietstack.model.answer;

import lombok.Data;

import java.util.Date;

@Data
public class Answer {

    private String aid;
    private String qid;
    private String uid;
    private Date date;
    private String status;

}
