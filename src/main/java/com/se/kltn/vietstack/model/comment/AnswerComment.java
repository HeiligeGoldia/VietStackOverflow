package com.se.kltn.vietstack.model.comment;

import lombok.Data;

import java.util.Date;

@Data
public class AnswerComment {

    private String caid;
    private String aid;
    private String uid;
    private Date date;
    private String detail;
    private String status;

}
