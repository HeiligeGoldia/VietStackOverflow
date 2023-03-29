package com.se.kltn.vietstack.model.comment;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {

    private String cid;
    private String qid;
    private String uid;
    private Date date;
    private String detail;
    private String status;

}
