package com.se.kltn.vietstack.model.comment;

import lombok.Data;

import java.util.Date;

@Data
public class CommentReport {

    private String rcid;
    private String cid;
    private String uid;
    private String detail;
    private Date date;
    private String status;

}
