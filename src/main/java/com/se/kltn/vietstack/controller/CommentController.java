package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.comment.Comment;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.CommentService;
import com.se.kltn.vietstack.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private QuestionService questionService;

    //    ---------- Comment ----------

    @PostMapping("/create/{qid}")
    public ResponseEntity<String> create(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody Comment comment)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Question q = questionService.getQuestionByQid(qid);
            if(q.getStatus().equals("Closed")){
                return ResponseEntity.ok("Question closed");
            }
            else {
                comment.setUid(user.getUid());
                comment.setQid(qid);
                comment.setDate(new Date());
                comment.setStatus("None");
                String s = commentService.createComment(comment);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getAnswerByQid/{qid}")
    public ResponseEntity<List<Comment>> getCommentByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<Comment> lc = commentService.getCommentByQid(qid);
        return ResponseEntity.ok(lc);
    }

    @DeleteMapping("/deleteComment/{cid}")
    public ResponseEntity<String> deleteComment(@CookieValue("sessionCookie") String ck, @PathVariable("cid") String cid){
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String s = commentService.deleteComment(cid);
            return ResponseEntity.ok(s);
        }
    }

    //    ---------- Comment Report ----------

}
