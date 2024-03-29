package com.se.kltn.vietstack.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.se.kltn.vietstack.model.answer.*;
import com.se.kltn.vietstack.model.comment.AnswerComment;
import com.se.kltn.vietstack.model.comment.AnswerCommentReport;
import com.se.kltn.vietstack.model.dto.AnswerActivityHistoryDTO;
import com.se.kltn.vietstack.model.dto.AnswerDTO;
import com.se.kltn.vietstack.model.dto.AnswerReportDTO;
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
@RequestMapping("/answer")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AnswerCommentService answerCommentService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionService questionService;

    //    ---------- Answer ----------

    @PostMapping("/create/{qid}/{sessionCookie}")
    public ResponseEntity<String> create(@PathVariable("sessionCookie") String ck, @PathVariable("qid") String qid)
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
                Answer answer = new Answer();
                answer.setUid(user.getUid());
                answer.setQid(qid);
                answer.setDate(new Date());
                answer.setStatus("None");
                String s = answerService.createAnswer(answer);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getAnswerDTOByQid/{qid}")
    public ResponseEntity<List<AnswerDTO>> getAnswerDTOByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<AnswerDTO> aadtl = new ArrayList<>();
        List<Answer> la = answerService.getAnswerByQid(qid);
        for(Answer a : la){
            AnswerDTO dto = new AnswerDTO();
            List<AnswerDetail> adl = answerService.getAnswerDetailByAid(a.getAid());
            List<AnswerComment> acl = answerCommentService.getAnswerCommentByAid(a.getAid());
            int av = answerService.getTotalVoteValue(a.getAid());
            User u = userService.findByUid(a.getUid());
            dto.setAnswerVote(av);
            dto.setUser(u);
            dto.setAnswer(a);
            dto.setAnswerDetails(adl);
            dto.setAnswerComments(acl);
            aadtl.add(dto);
        }
        return ResponseEntity.ok(aadtl);
    }

    @GetMapping("/getAnswerDTOByQidCk/{qid}/{sessionCookie}")
    public ResponseEntity<List<AnswerDTO>> getAnswerDTOByQidCk(@PathVariable("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            List<AnswerDTO> aadtl = new ArrayList<>();
            List<Answer> la = answerService.getAnswerByQid(qid);
            for(Answer a : la){
                AnswerDTO dto = new AnswerDTO();
                List<AnswerDetail> adl = answerService.getAnswerDetailByAid(a.getAid());
                List<AnswerComment> acl = answerCommentService.getAnswerCommentByAid(a.getAid());
                int av = answerService.getTotalVoteValue(a.getAid());
                User u = userService.findByUid(a.getUid());
                String vv = answerService.getUserVoteValue(user.getUid(), a.getAid());
                dto.setAnswerVote(av);
                dto.setUser(u);
                dto.setAnswer(a);
                dto.setAnswerDetails(adl);
                dto.setAnswerComments(acl);
                dto.setVoteValue(vv);
                aadtl.add(dto);
            }
            return ResponseEntity.ok(aadtl);
        }
    }

    @GetMapping("/getAnswerByAid/{aid}")
    public ResponseEntity<Answer> getAnswerByAid(@PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        Answer a = answerService.getAnswerByAid(aid);
        return ResponseEntity.ok(a);
    }

    @GetMapping("/getAnswerByQid/{qid}")
    public ResponseEntity<List<Answer>> getAnswerByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<Answer> la = answerService.getAnswerByQid(qid);
        return ResponseEntity.ok(la);
    }

    @PutMapping("/acceptAnswer/{aid}/{sessionCookie}")
    public ResponseEntity<String> acceptAnswer(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String s = answerService.acceptAnswer(aid);
            return ResponseEntity.ok(s);
        }
    }

    @DeleteMapping("/deleteAnswer/{aid}/{sessionCookie}")
    public ResponseEntity<String> deleteAnswer(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            Answer a = answerService.getAnswerByAid(aid);
            if (!a.getUid().equals(user.getUid()) && !role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            } else {
                List<AnswerReport> arl = answerService.getReportByAid(aid);
                for(AnswerReport ar : arl) {
                    ar.setAid("Câu trả lời đã bị xoá");
                    ar.setStatus("Đã xoá");
                    answerService.editReport(ar);
                }

                List<AnswerComment> acl = answerCommentService.getAnswerCommentByAid(aid);
                for(AnswerComment ac : acl) {
                    List<AnswerCommentReport> acrl = answerCommentService.getReportByCaid(ac.getCaid());
                    for(AnswerCommentReport acr : acrl) {
                        acr.setCaid("Bình luận đã bị xoá");
                        acr.setStatus("Đã xoá");
                        answerCommentService.editReport(acr);
                    }
                    answerCommentService.deleteAnswerComment(ac.getCaid());
                }

                answerService.deleteHistoryByAid(aid);
                answerService.removeAnswerVoteByAid(aid);
                answerService.removeAllDetailByAid(aid);
                String s = answerService.deleteAnswer(aid);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getTotalAnswer/{sessionCookie}")
    public ResponseEntity<Integer> getTotalAnswer(@PathVariable("sessionCookie") String ck)
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
                int sl = answerService.getSlAnswerTotal();
                return ResponseEntity.ok(sl);
            }
        }
    }

    @GetMapping("/getTotalAnswerYear/{year}/{sessionCookie}")
    public ResponseEntity<HashMap> getTotalAnswerYear(@PathVariable("sessionCookie") String ck, @PathVariable("year") int year)
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
                HashMap hm = answerService.getSlAnswerInYear(year);
                return ResponseEntity.ok(hm);
            }
        }
    }

    //    ---------- Answer Detail ----------

    @PostMapping("/createDetail/{aid}/{sessionCookie}")
    public ResponseEntity<String> createDetail(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid, @RequestBody List<AnswerDetail> answerDetailList)
            throws ExecutionException, InterruptedException {
        if(answerDetailList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Detail list empty");
        }
        else {
            User user = accountService.verifySC(ck);
            if(user.getUid()==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
            }
            else {
                int i = 0;
                for(AnswerDetail ad : answerDetailList){
                    ad.setAid(aid);
                    answerService.createDetail(ad);
                    i++;
                }
                return ResponseEntity.ok(String.valueOf(i));
            }
        }
    }

    @PostMapping("/editDetail/{aid}/{sessionCookie}")
    public ResponseEntity<String> editDetail(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid, @RequestBody List<AnswerDetail> answerDetailList)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(answerDetailList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Detail list empty");
        }
        else {
            if(user.getUid()==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
            }
            else {
                Answer a = answerService.getAnswerByAid(aid);
                if(!a.getUid().equals(user.getUid())){
                    return ResponseEntity.ok("Access denied");
                }
                else {
                    answerService.removeAllDetailByAid(aid);
                    int i = 0;
                    for(AnswerDetail ad : answerDetailList){
                        ad.setAid(aid);
                        answerService.createDetail(ad);
                        i++;
                    }
                    return ResponseEntity.ok(String.valueOf(i));
                }
            }
        }
    }

    @GetMapping("/getAnswerDetailByAid/{aid}")
    public ResponseEntity<List<AnswerDetail>> getAnswerDetailByAid(@PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        List<AnswerDetail> adl = answerService.getAnswerDetailByAid(aid);
        return ResponseEntity.ok(adl);
    }

    //    ---------- Answer Vote ----------

    @PostMapping("/castAnswerVoteUD/{aid}/{sessionCookie}")
    public ResponseEntity<String> castQuestionVoteUD(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid, @RequestBody AnswerVote answerVote)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            answerVote.setAid(aid);
            answerVote.setUid(user.getUid());
            String s = answerService.castAnswerVote(answerVote);
            return ResponseEntity.ok(s);
        }
    }

    @GetMapping("/getTotalVoteValue/{aid}")
    public ResponseEntity<String> getTotalVoteValue(@PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        int i = answerService.getTotalVoteValue(aid);
        return ResponseEntity.ok(String.valueOf(i));
    }

    @GetMapping("/getUserVoteValue/{aid}/{sessionCookie}")
    public ResponseEntity<String> getUserVoteValue(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String val = answerService.getUserVoteValue(user.getUid(), aid);
            return ResponseEntity.ok(val);
        }
    }

    @GetMapping("/getTotalVote/{sessionCookie}")
    public ResponseEntity<Integer> getTotalVote(@PathVariable("sessionCookie") String ck) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if (user.getUid() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else {
                int sl = answerService.getTotalVote();
                return ResponseEntity.ok(sl);
            }
        }
    }

    //    ---------- Answer Activity History ----------

    @GetMapping("/getAnswerActivityHistory/{aid}")
    public ResponseEntity<List<AnswerActivityHistoryDTO>> getAnswerActivityHistory(@PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        List<AnswerActivityHistory> al = answerService.getAnswerActivityHistory(aid);
        List<AnswerActivityHistoryDTO> dtoList = new ArrayList<>();
        for (AnswerActivityHistory a : al){
            AnswerActivityHistoryDTO dto = new AnswerActivityHistoryDTO();
            dto.setAnswerActivityHistory(a);
            dto.setUser(userService.findByUid(a.getUid()));
            dtoList.add(dto);
        }
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/createActivityHistory/{aid}/{sessionCookie}")
    public ResponseEntity<String> createActivityHistory(@PathVariable("aid") String aid, @PathVariable("sessionCookie") String ck, @RequestBody AnswerActivityHistory answerActivityHistory)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            answerActivityHistory.setAid(aid);
            answerActivityHistory.setUid(user.getUid());
            answerActivityHistory.setDate(new Date());
            String s = answerService.createActivityHistory(answerActivityHistory);
            return ResponseEntity.ok(s);
        }
    }

    //    ---------- Answer Report ----------

    @PostMapping("/report/{aid}/{sessionCookie}")
    public ResponseEntity<String> report(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid, @RequestBody AnswerReport report) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Answer a = answerService.getAnswerByAid(aid);
            if(a.getUid().equals(user.getUid())){
                return ResponseEntity.ok("Can not report your own comment");
            }
            else {
                report.setUid(user.getUid());
                report.setAid(aid);
                report.setStatus("Đang chờ xử lý");
                report.setDate(new Date());
                String s = answerService.report(report);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getUserReportValue/{aid}/{sessionCookie}")
    public ResponseEntity<String> getUserReportValue(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            AnswerReport ar = answerService.getReportByUidAid(user.getUid(), aid);
            if(ar.getRaid()==null){
                return ResponseEntity.ok("None");
            }
            else {
                return ResponseEntity.ok(ar.getRaid());
            }
        }
    }

    @PutMapping("/editReport/{sessionCookie}")
    public ResponseEntity<String> editReport(@PathVariable("sessionCookie") String ck, @RequestBody List<String> arlid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            } else {
                for(String id : arlid){
                    AnswerReport a = answerService.getReportByRaid(id);
                    if(!a.getAid().equals("Câu trả lời đã bị xoá")){
                        a.setStatus("Đã xem xét");
                        answerService.editReport(a);
                    }
                }
                return ResponseEntity.ok("Edited");
            }
        }
    }

    @DeleteMapping("/deleteReport/{raid}/{sessionCookie}")
    public ResponseEntity<String> deleteReport(@PathVariable("sessionCookie") String ck, @PathVariable("raid") String raid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            AnswerReport a = answerService.getReportByRaid(raid);
            if(!a.getUid().equals(user.getUid()) && !role.equals("Admin")){
                return ResponseEntity.ok("Access denied");
            }
            else {
                String s = answerService.deleteReport(raid);
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
            for (String raid : ids) {
                AnswerReport a = answerService.getReportByRaid(raid);
                if(!a.getUid().equals(user.getUid()) && !role.equals("Admin")){
                    return ResponseEntity.ok("Access denied");
                }
                else {
                    answerService.deleteReport(raid);
                }
            }
            return ResponseEntity.ok("All report deleted");
        }
    }

    @GetMapping("/getAnswerReport/{sessionCookie}")
    public ResponseEntity<List<AnswerReportDTO>> getAnswerReport(@PathVariable("sessionCookie") String ck) throws ExecutionException, InterruptedException, FirebaseAuthException {
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
                List<AnswerReport> rl = answerService.getAnswerReport();
                List<AnswerReportDTO> dtoList = new ArrayList<>();
                for (AnswerReport r : rl){
                    AnswerReportDTO dto = new AnswerReportDTO();
                    dto.setAnswerReport(r);
                    if(!r.getAid().equals("Câu trả lời đã bị xoá")){
                        Answer a = answerService.getAnswerByAid(r.getAid());
                        dto.setQuestion(questionService.getQuestionByQid(a.getQid()));
                    }
                    dto.setUser(userService.findByUid(r.getUid()));
                    dtoList.add(dto);
                }
                return ResponseEntity.ok(dtoList);
            }
        }
    }

    @GetMapping("/getReportByAid/{aid}/{sessionCookie}")
    public ResponseEntity<List<AnswerReportDTO>> getReportByAid(@PathVariable("sessionCookie") String ck, @PathVariable("aid") String aid) throws ExecutionException, InterruptedException, FirebaseAuthException {
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
                List<AnswerReport> rl = answerService.getReportByAid(aid);
                List<AnswerReportDTO> dtoList = new ArrayList<>();
                for (AnswerReport r : rl){
                    AnswerReportDTO dto = new AnswerReportDTO();
                    dto.setAnswerReport(r);
                    if(!r.getAid().equals("Câu trả lời đã bị xoá")){
                        Answer a = answerService.getAnswerByAid(r.getAid());
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
                List<AnswerReport> rl = answerService.getUserReport(uid);
                if(rl.isEmpty()){
                    return ResponseEntity.ok("List report empty");
                }
                else {
                    List<AnswerReportDTO> dtoList = new ArrayList<>();
                    for (AnswerReport r : rl){
                        AnswerReportDTO dto = new AnswerReportDTO();
                        dto.setAnswerReport(r);
                        if(!r.getAid().equals("Câu trả lời đã bị xoá")){
                            Answer a = answerService.getAnswerByAid(r.getAid());
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

    @GetMapping("/getTotalAnswerReport/{sessionCookie}")
    public ResponseEntity<Integer> getTotalAnswerReport(@PathVariable("sessionCookie") String ck)
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
                int sl = answerService.getSlAnswerReportTotal();
                return ResponseEntity.ok(sl);
            }
        }
    }

    @GetMapping("/getTotalAnswerReportYear/{year}/{sessionCookie}")
    public ResponseEntity<HashMap> getTotalAnswerReportYear(@PathVariable("sessionCookie") String ck, @PathVariable("year") int year)
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
                HashMap hm = answerService.getSlAnswerReportInYear(year);
                return ResponseEntity.ok(hm);
            }
        }
    }

}
