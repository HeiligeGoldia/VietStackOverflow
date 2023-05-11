package com.se.kltn.vietstack.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.se.kltn.vietstack.model.comment.Comment;
import com.se.kltn.vietstack.model.comment.CommentReport;
import com.se.kltn.vietstack.model.dto.CommentDTO;
import com.se.kltn.vietstack.model.dto.CommentReportDTO;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.CommentService;
import com.se.kltn.vietstack.service.QuestionService;
import com.se.kltn.vietstack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private UserService userService;

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

    @PutMapping("/edit/{cid}")
    public ResponseEntity<String> edit(@CookieValue("sessionCookie") String ck, @PathVariable("cid") String cid, @RequestBody Comment comment) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Comment c = commentService.getCommentByCid(cid);
            if(!c.getUid().equals(user.getUid())){
                return ResponseEntity.ok("Access denied");
            }
            else {
                c.setDate(new Date());
                c.setDetail(comment.getDetail());
                c.setStatus("Modified");
                String s = commentService.editComment(c);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getCommentByQid/{qid}")
    public ResponseEntity<List<Comment>> getCommentByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<Comment> lc = commentService.getCommentByQid(qid);
        return ResponseEntity.ok(lc);
    }

    @GetMapping("/getCommentDTOByQid/{qid}")
    public ResponseEntity<List<CommentDTO>> getCommentDTOByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<CommentDTO> dtoList = new ArrayList<>();
        List<Comment> lc = commentService.getCommentByQid(qid);
        for (Comment c : lc){
            User u = userService.findByUid(c.getUid());
            CommentDTO dto = new CommentDTO();
            dto.setComment(c);
            dto.setUser(u);
            dtoList.add(dto);
        }
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/deleteComment/{cid}")
    public ResponseEntity<String> deleteComment(@CookieValue("sessionCookie") String ck, @PathVariable("cid") String cid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Comment c = commentService.getCommentByCid(cid);
            String role = accountService.getUserClaims(ck);
            if(!c.getUid().equals(user.getUid()) && !role.equals("Admin")){
                return ResponseEntity.ok("Access denied");
            }
            else {
                String s = commentService.deleteComment(cid);
                return ResponseEntity.ok(s);
            }
        }
    }

    //    ---------- Comment Report ----------

    @PostMapping("/report/{cid}")
    public ResponseEntity<String> report(@CookieValue("sessionCookie") String ck, @PathVariable("cid") String cid, @RequestBody CommentReport report) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Comment c = commentService.getCommentByCid(cid);
            if(c.getUid().equals(user.getUid())){
                return ResponseEntity.ok("Can not report your own comment");
            }
            else {
                report.setUid(user.getUid());
                report.setCid(cid);
                report.setStatus("Pending");
                report.setDate(new Date());
                String s = commentService.report(report);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getUserReportValue/{cid}")
    public ResponseEntity<String> getUserReportValue(@CookieValue("sessionCookie") String ck, @PathVariable("cid") String cid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            CommentReport cr = commentService.getReportByUidCid(user.getUid(), cid);
            if(cr.getRcid()==null){
                return ResponseEntity.ok("None");
            }
            else {
                return ResponseEntity.ok(cr.getRcid());
            }
        }
    }

    @DeleteMapping("/deleteReport/{rcid}")
    public ResponseEntity<String> deleteReport(@CookieValue("sessionCookie") String ck, @PathVariable("rcid") String rcid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            CommentReport c = commentService.getReportByRcid(rcid);
            if(!c.getUid().equals(user.getUid()) && !role.equals("Admin")){
                return ResponseEntity.ok("Access denied");
            }
            else {
                String s = commentService.deleteReport(rcid);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getUserReport")
    public ResponseEntity<List<CommentReportDTO>> getUserReport(@CookieValue("sessionCookie") String ck) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            List<CommentReport> rl = commentService.getUserReport(user.getUid());
            List<CommentReportDTO> dtoList = new ArrayList<>();
            for (CommentReport r : rl){
                CommentReportDTO dto = new CommentReportDTO();
                dto.setCommentReport(r);
                dto.setUser(userService.findByUid(user.getUid()));
                dtoList.add(dto);
            }
            return ResponseEntity.ok(dtoList);
        }
    }

}
