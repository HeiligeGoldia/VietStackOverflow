package com.se.kltn.vietstack.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.comment.AnswerComment;
import com.se.kltn.vietstack.model.comment.AnswerCommentReport;
import com.se.kltn.vietstack.model.dto.AnswerCommentDTO;
import com.se.kltn.vietstack.model.dto.AnswerCommentReportDTO;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/answerComment")
public class AnswerCommentController {

    @Autowired
    private AnswerCommentService answerCommentService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    //    ---------- Answer Comment ----------

    @PostMapping("/create/{aid}/{sessionCookie}")
    public ResponseEntity<String> create(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid, @RequestBody AnswerComment answerComment)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Answer a = answerService.getAnswerByAid(aid);
            Question q = questionService.getQuestionByQid(a.getQid());
            if(q.getStatus().equals("Closed")){
                return ResponseEntity.ok("Question closed");
            }
            else {
                answerComment.setUid(user.getUid());
                answerComment.setAid(aid);
                answerComment.setDate(new Date());
                answerComment.setStatus("None");
                String s = answerCommentService.createAnswerComment(answerComment);
                return ResponseEntity.ok(s);
            }
        }
    }

    @PutMapping("/edit/{caid}/{sessionCookie}")
    public ResponseEntity<String> edit(@PathVariable("sessionCookie") String ck, @PathVariable("caid") String caid, @RequestBody AnswerComment comment) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            AnswerComment c = answerCommentService.getAnswerCommentByCaid(caid);
            if(!c.getUid().equals(user.getUid())){
                return ResponseEntity.ok("Access denied");
            }
            else {
                c.setDetail(comment.getDetail());
                c.setStatus("Modified");
                String s = answerCommentService.editAnswerComment(c);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getCommentByAid/{aid}")
    public ResponseEntity<List<AnswerComment>> getCommentByQid(@PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        List<AnswerComment> lc = answerCommentService.getAnswerCommentByAid(aid);
        return ResponseEntity.ok(lc);
    }

    @GetMapping("/getCommentDTOByAid/{aid}")
    public ResponseEntity<List<AnswerCommentDTO>> getCommentDTOByQid(@PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        List<AnswerCommentDTO> dtoList = new ArrayList<>();
        List<AnswerComment> lc = answerCommentService.getAnswerCommentByAid(aid);
        for (AnswerComment c : lc){
            User u = userService.findByUid(c.getUid());
            AnswerCommentDTO dto = new AnswerCommentDTO();
            dto.setAnswerComment(c);
            dto.setUser(u);
            dtoList.add(dto);
        }
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping("/deleteComment/{caid}/{sessionCookie}")
    public ResponseEntity<String> deleteComment(@PathVariable("sessionCookie") String ck, @PathVariable("caid") String caid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            AnswerComment c = answerCommentService.getAnswerCommentByCaid(caid);
            String role = accountService.getUserClaims(ck);
            if(!c.getUid().equals(user.getUid()) && !role.equals("Admin")){
                return ResponseEntity.ok("Access denied");
            }
            else {
                List<AnswerCommentReport> crl = answerCommentService.getReportByCaid(caid);
                for(AnswerCommentReport cr : crl) {
                    cr.setCaid("Bình luận đã bị xoá");
                    cr.setStatus("Đã xoá");
                    answerCommentService.editReport(cr);
                }
                String s = answerCommentService.deleteAnswerComment(caid);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getTotalComment/{sessionCookie}")
    public ResponseEntity<Integer> getTotalComment(@PathVariable("sessionCookie") String ck)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if (user.getUid() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                int sl = answerCommentService.getSlAnswerCommentTotal();
                return ResponseEntity.ok(sl);
            }
        }
    }

    @GetMapping("/getTotalCommentYear/{year}/{sessionCookie}")
    public ResponseEntity<HashMap> getTotalCommentYear(@PathVariable("sessionCookie") String ck, @PathVariable("year") int year)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if (user.getUid() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                HashMap hm = answerCommentService.getSlAnswerCommentInYear(year);
                return ResponseEntity.ok(hm);
            }
        }
    }

    //    ---------- Answer Comment Report ----------

    @PostMapping("/report/{caid}/{sessionCookie}")
    public ResponseEntity<String> report(@PathVariable("sessionCookie") String ck, @PathVariable("caid") String caid, @RequestBody AnswerCommentReport report) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            AnswerComment c = answerCommentService.getAnswerCommentByCaid(caid);
            if(c.getUid().equals(user.getUid())){
                return ResponseEntity.ok("Can not report your own comment");
            }
            else {
                report.setUid(user.getUid());
                report.setCaid(caid);
                report.setStatus("Đang chờ xử lý");
                report.setDate(new Date());
                String s = answerCommentService.report(report);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getUserReportValue/{caid}/{sessionCookie}")
    public ResponseEntity<String> getUserReportValue(@PathVariable("sessionCookie") String ck, @PathVariable("caid") String caid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            AnswerCommentReport cr = answerCommentService.getReportByUidCaid(user.getUid(), caid);
            if(cr.getRcaid()==null){
                return ResponseEntity.ok("None");
            }
            else {
                return ResponseEntity.ok(cr.getRcaid());
            }
        }
    }

    @PutMapping("/editReport/{sessionCookie}")
    public ResponseEntity<String> editReport(@PathVariable("sessionCookie") String ck, @RequestBody List<String> crlid) throws FirebaseAuthException, ExecutionException, InterruptedException {
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
                    AnswerCommentReport c = answerCommentService.getReportByRcaid(id);
                    if(!c.getCaid().equals("Bình luận đã bị xoá")){
                        c.setStatus("Đã xem xét");
                        answerCommentService.editReport(c);
                    }
                }
                return ResponseEntity.ok("Edited");
            }
        }
    }

    @DeleteMapping("/deleteReport/{rcaid}/{sessionCookie}")
    public ResponseEntity<String> deleteReport(@PathVariable("sessionCookie") String ck, @PathVariable("rcaid") String rcaid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            AnswerCommentReport c = answerCommentService.getReportByRcaid(rcaid);
            if(!c.getUid().equals(user.getUid()) && !role.equals("Admin")){
                return ResponseEntity.ok("Access denied");
            }
            else {
                String s = answerCommentService.deleteReport(rcaid);
                return ResponseEntity.ok(s);
            }
        }
    }

    @DeleteMapping("/deleteListReport/{sessionCookie}")
    public ResponseEntity<String> deleteListReport(@PathVariable("sessionCookie") String ck, @RequestBody List<String> ids) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            for (String rcid : ids) {
                AnswerCommentReport c = answerCommentService.getReportByRcaid(rcid);
                if(!c.getUid().equals(user.getUid()) && !role.equals("Admin")){
                    return ResponseEntity.ok("Access denied");
                }
                else {
                    answerCommentService.deleteReport(rcid);
                }
            }
            return ResponseEntity.ok("All report deleted");
        }
    }

    @GetMapping("/getCommentReport/{sessionCookie}")
    public ResponseEntity<List<AnswerCommentReportDTO>> getCommentReport(@PathVariable("sessionCookie") String ck) throws ExecutionException, InterruptedException, FirebaseAuthException {
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
                List<AnswerCommentReport> rl = answerCommentService.getAnswerCommentReport();
                List<AnswerCommentReportDTO> dtoList = new ArrayList<>();
                for (AnswerCommentReport r : rl){
                    AnswerCommentReportDTO dto = new AnswerCommentReportDTO();
                    dto.setAnswerCommentReport(r);
                    if(!r.getCaid().equals("Bình luận đã bị xoá")) {
                        AnswerComment c = answerCommentService.getAnswerCommentByCaid(r.getCaid());
                        dto.setAnswerComment(c);
                        Answer a = answerService.getAnswerByAid(c.getAid());
                        dto.setAnswer(a);
                        dto.setQuestion(questionService.getQuestionByQid(a.getQid()));
                    }
                    dto.setUser(userService.findByUid(r.getUid()));
                    dtoList.add(dto);
                }
                return ResponseEntity.ok(dtoList);
            }
        }
    }

    @GetMapping("/getReportByCaid/{caid}/{sessionCookie}")
    public ResponseEntity<List<AnswerCommentReportDTO>> getReportByCaid(@PathVariable("sessionCookie") String ck, @PathVariable("caid") String caid) throws ExecutionException, InterruptedException, FirebaseAuthException {
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
                List<AnswerCommentReport> rl = answerCommentService.getReportByCaid(caid);
                List<AnswerCommentReportDTO> dtoList = new ArrayList<>();
                for (AnswerCommentReport r : rl){
                    AnswerCommentReportDTO dto = new AnswerCommentReportDTO();
                    dto.setAnswerCommentReport(r);
                    if(!r.getCaid().equals("Bình luận đã bị xoá")) {
                        AnswerComment c = answerCommentService.getAnswerCommentByCaid(r.getCaid());
                        dto.setAnswerComment(c);
                        Answer a = answerService.getAnswerByAid(c.getAid());
                        dto.setAnswer(a);
                        dto.setQuestion(questionService.getQuestionByQid(a.getQid()));
                    }
                    dto.setUser(userService.findByUid(r.getUid()));
                    dtoList.add(dto);
                }
                return ResponseEntity.ok(dtoList);
            }
        }
    }

    @GetMapping("/getUserReport/{uid}/{sessionCookie}")
    public ResponseEntity<?> getUserReport(@PathVariable("sessionCookie") String ck, @PathVariable("uid") String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
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
                List<AnswerCommentReport> rl = answerCommentService.getUserReport(uid);
                if(rl.isEmpty()){
                    return ResponseEntity.ok("List report empty");
                }
                else {
                    List<AnswerCommentReportDTO> dtoList = new ArrayList<>();
                    for (AnswerCommentReport r : rl){
                        AnswerCommentReportDTO dto = new AnswerCommentReportDTO();
                        dto.setAnswerCommentReport(r);
                        if(!r.getCaid().equals("Bình luận đã bị xoá")) {
                            AnswerComment c = answerCommentService.getAnswerCommentByCaid(r.getCaid());
                            dto.setAnswerComment(c);
                            Answer a = answerService.getAnswerByAid(c.getAid());
                            dto.setAnswer(a);
                            dto.setQuestion(questionService.getQuestionByQid(a.getQid()));
                        }
                        dto.setUser(userService.findByUid(r.getUid()));
                        dtoList.add(dto);
                    }
                    return ResponseEntity.ok(dtoList);
                }
            }
        }
    }

    @GetMapping("/getTotalCommentReport/{sessionCookie}")
    public ResponseEntity<Integer> getTotalCommentReport(@PathVariable("sessionCookie") String ck)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if (user.getUid() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                int sl = answerCommentService.getSlAnswerCommentReportTotal();
                return ResponseEntity.ok(sl);
            }
        }
    }

    @GetMapping("/getTotalCommentReportYear/{year}/{sessionCookie}")
    public ResponseEntity<HashMap> getTotalCommentReportYear(@PathVariable("sessionCookie") String ck, @PathVariable("year") int year)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if (user.getUid() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                HashMap hm = answerCommentService.getSlAnswerCommentReportInYear(year);
                return ResponseEntity.ok(hm);
            }
        }
    }

}
