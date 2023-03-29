package com.se.kltn.vietstack.model.answer;

import lombok.Data;

import java.util.Date;

@Data
public class AnswerActivityHistory {

    private String aahid;
    private String aid;
    private String uid;
    private String action;
    private String description;
    private Date date;

}
