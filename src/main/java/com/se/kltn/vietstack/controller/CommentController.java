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
                List<CommentReport> crl = commentService.getReportByCid(cid);
                for(CommentReport cr : crl) {
                    cr.setCid("Bình luận đã bị xoá");
                    cr.setStatus("Đã xoá");
                    commentService.editReport(cr);
                }
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
                report.setStatus("Đang chờ xử lý");
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

    @PutMapping("/editReport")
    public ResponseEntity<String> editReport(@CookieValue("sessionCookie") String ck, @RequestBody List<String> crlid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            } else {
                for(String id : crlid){
                    CommentReport c = commentService.getReportByRcid(id);
                    if(!c.getCid().equals("Bình luận đã bị xoá")){
                        c.setStatus("Đã xem xét");
                        commentService.editReport(c);
                    }
                }
                return ResponseEntity.ok("Edited");
            }
        }
    }

    @DeleteMapping("/deleteReport")
    public ResponseEntity<String> deleteReport(@CookieValue("sessionCookie") String ck, @RequestBody List<String> ids) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            for(String rcid : ids) {
                CommentReport c = commentService.getReportByRcid(rcid);
                if(!c.getUid().equals(user.getUid()) && !role.equals("Admin")){
                    return ResponseEntity.ok("Access denied");
                }
                else {
                    commentService.deleteReport(rcid);
                }
            }
            return ResponseEntity.ok("All report deleted");
        }
    }

    @GetMapping("/getCommentReport")
    public ResponseEntity<List<CommentReportDTO>> getCommentReport(@CookieValue("sessionCookie") String ck) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                List<CommentReport> rl = commentService.getCommentReport();
                List<CommentReportDTO> dtoList = new ArrayList<>();
                for (CommentReport r : rl){
                    CommentReportDTO dto = new CommentReportDTO();
                    dto.setCommentReport(r);
                    dto.setComment(commentService.getCommentByCid(r.getCid()));
                    dto.setUser(userService.findByUid(r.getUid()));
                    dtoList.add(dto);
                }
                return ResponseEntity.ok(dtoList);
            }
        }
    }

    @GetMapping("/getReportByCid/{cid}")
    public ResponseEntity<List<CommentReportDTO>> getReportByCid(@CookieValue("sessionCookie") String ck, @PathVariable("cid") String cid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                List<CommentReport> rl = commentService.getReportByCid(cid);
                List<CommentReportDTO> dtoList = new ArrayList<>();
                for (CommentReport r : rl){
                    CommentReportDTO dto = new CommentReportDTO();
                    dto.setCommentReport(r);
                    dto.setComment(commentService.getCommentByCid(r.getCid()));
                    dto.setUser(userService.findByUid(r.getUid()));
                    dtoList.add(dto);
                }
                return ResponseEntity.ok(dtoList);
            }
        }
    }

    @GetMapping("/getUserReport/{uid}")
    public ResponseEntity<?> getUserReport(@CookieValue("sessionCookie") String ck, @PathVariable("uid") String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!user.getUid().equals(uid) && !role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                List<CommentReport> rl = commentService.getUserReport(uid);
                if(rl.isEmpty()){
                    return ResponseEntity.ok("List report empty");
                }
                else {
                    List<CommentReportDTO> dtoList = new ArrayList<>();
                    for (CommentReport r : rl){
                        CommentReportDTO dto = new CommentReportDTO();
                        dto.setCommentReport(r);
                        dto.setComment(commentService.getCommentByCid(r.getCid()));
                        dto.setUser(userService.findByUid(r.getUid()));
                        dtoList.add(dto);
                    }
                    return ResponseEntity.ok(dtoList);
                }
            }
        }
    }

}
