package com.se.kltn.vietstack.model.dto;

import com.se.kltn.vietstack.model.comment.Comment;
import com.se.kltn.vietstack.model.user.User;
import lombok.Data;

@Data
public class CommentDTO {

    private Comment comment;
    private User user;

}
