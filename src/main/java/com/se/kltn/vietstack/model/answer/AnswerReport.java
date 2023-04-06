package com.se.kltn.vietstack.model.answer;

import lombok.Data;

import java.util.Date;

@Data
public class AnswerReport {

    private String raid;
    private String aid;
    private String uid;
    private String detail;
    private Date date;
    private String status;

}
