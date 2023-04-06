package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.dto.QuestionActivityHistoryDTO;
import com.se.kltn.vietstack.model.dto.QuestionDTO;
import com.se.kltn.vietstack.model.dto.QuestionReportDTO;
import com.se.kltn.vietstack.model.question.*;
import com.se.kltn.vietstack.model.tag.Tag;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    //    ---------- Question ----------

    @PostMapping("/create")
    public ResponseEntity<String> create(@CookieValue("sessionCookie") String ck, @RequestBody Question question) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            question.setUid(user.getUid());
            question.setStatus("Open");
            question.setDate(new Date());
            String s = questionService.create(question);
            return ResponseEntity.ok(s);
        }
    }

    @GetMapping("/getAllQuestionList")
    public ResponseEntity<List<Question>> getAllQuestionList() throws ExecutionException, InterruptedException {
        List<Question> ql = questionService.getAllQuestionList();
        return ResponseEntity.ok(ql);
    }

    @GetMapping("/getQuestionById/{qid}")
    public ResponseEntity<Question> getQuestionById(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        Question q = questionService.getQuestionByQid(qid);
        if(q.getQid()==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(q);
        }
        else {
            return ResponseEntity.ok(q);
        }
    }

    @PutMapping("/closeQuestion/{qid}")
    public ResponseEntity<String> closeQuestion(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            List<Answer> al = answerService.getAcceptAnswerByQid(qid);
            if(al.isEmpty()){
                String s = questionService.closeQuestion(qid, false);
                return ResponseEntity.ok(s);
            }
            else {
                String s = questionService.closeQuestion(qid, true);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getAllQuestionDTO")
    public ResponseEntity<List<QuestionDTO>> getAllQuestionDTO() throws ExecutionException, InterruptedException {
        List<QuestionDTO> dtoList = new ArrayList<>();
        List<Question> questions = questionService.getAllQuestionList();
        for (Question q : questions){
            QuestionDTO questionDTO = new QuestionDTO();
            List<Tag> tags = new ArrayList<>();
            List<QuestionTag> qtags = questionService.getQuestionTagByQid(q.getQid());
            for (QuestionTag qt : qtags){
                 tags.add(tagService.getTagByTid(qt.getTid()));
            }
            int qv = questionService.getTotalVoteValue(q.getQid());
            int ac = answerService.getTotalAnswerCountByQid(q.getQid());
            User u = userService.findByUid(q.getUid());
            questionDTO.setQuestion(q);
            questionDTO.setTags(tags);
            questionDTO.setQuestionVote(qv);
            questionDTO.setAnswerCount(ac);
            questionDTO.setUser(u);
            dtoList.add(questionDTO);
        }
        return ResponseEntity.ok(dtoList);
    }

    //    ---------- Question Tag ----------

    @PostMapping("/modifyTagPost/{qid}")
    public ResponseEntity<String> addTagToPost(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody List<Tag> tags)
            throws ExecutionException, InterruptedException {
        if(tags.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Tag list empty");
        }
        else {
            User user = accountService.verifySC(ck);
            if(user.getUid()==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
            }
            else {
                int i = 0;
                int y = 0;
                List<QuestionTag> tag = questionService.getQuestionTagByQid(qid);
                List<String> tagIdOld = new ArrayList<>();
                for(QuestionTag qt1 : tag){
                    tagIdOld.add(qt1.getTid());
                }
                List<String> tagIdNew = new ArrayList<>();
                for(Tag qt2 : tags){
                    tagIdNew.add(qt2.getTid());
                }

                List<QuestionTag> newTags = new ArrayList<>();
                for(String s1 : tagIdNew){
                    if(!tagIdOld.contains(s1)){
                        QuestionTag obj1 = new QuestionTag();
                        obj1.setTid(s1);
                        newTags.add(obj1);
                    }
                }
                List<String> removedTags = new ArrayList<>();
                for(String s2 : tagIdOld){
                    if(!tagIdNew.contains(s2)){
                        removedTags.add(s2);
                    }
                }

                for (QuestionTag qt1 : newTags){
                    qt1.setQid(qid);
                    questionService.addTagToPost(qt1);
                    i++;
                }
                for (String qt2 : removedTags){
                    QuestionTag rmt = questionService.getQuestionTagByQidTid(qid, qt2);
                    questionService.removeQuestionTag(rmt);
                    y++;
                }
                return ResponseEntity.ok("Added tag(s): " + i + " - Removed tag(s): " + y);
            }
        }
    }

    @GetMapping("/getQuestionTagByQid/{qid}")
    public ResponseEntity<List<Tag>> getQuestionTagByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<Tag> tags = new ArrayList<>();
        List<QuestionTag> qtl = questionService.getQuestionTagByQid(qid);
        for (QuestionTag qt : qtl){
            tags.add(tagService.getTagByTid(qt.getTid()));
        }
        return ResponseEntity.ok(tags);
    }

    //    ---------- Question Detail ----------

    @PostMapping("/createDetail/{qid}")
    public ResponseEntity<String> createDetail(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody List<QuestionDetail> questionDetailList)
            throws ExecutionException, InterruptedException {
        if(questionDetailList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Detail list empty");
        }
        else {
            User user = accountService.verifySC(ck);
            if(user.getUid()==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
            }
            else {
                int i = 0;
                for(QuestionDetail qd : questionDetailList){
                    qd.setQid(qid);
                    questionService.createDetail(qd);
                    i++;
                }
                return ResponseEntity.ok(String.valueOf(i));
            }
        }
    }

    @GetMapping("/getQuestionDetailByQid/{qid}")
    public ResponseEntity<List<QuestionDetail>> getQuestionDetailByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<QuestionDetail> qdl = questionService.getQuestionDetailByQid(qid);
        return ResponseEntity.ok(qdl);
    }

    //    ---------- Question Vote ----------

    @PostMapping("/castQuestionVoteUD/{qid}")
    public ResponseEntity<String> castQuestionVoteUD(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody QuestionVote questionVote)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            questionVote.setQid(qid);
            questionVote.setUid(user.getUid());
            String s = questionService.castQuestionVote(questionVote);
            return ResponseEntity.ok(s);
        }
    }

    @GetMapping("/getTotalVoteValue/{qid}")
    public ResponseEntity<String> getTotalVoteValue(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        int i = questionService.getTotalVoteValue(qid);
        return ResponseEntity.ok(String.valueOf(i));
    }

    @GetMapping("/getUserVoteValue/{qid}")
    public ResponseEntity<String> getUserVoteValue(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String val = questionService.getUserVoteValue(user.getUid(), qid);
            return ResponseEntity.ok(val);
        }
    }

    //    ---------- Question Activity History ----------

    @GetMapping("/getQuestionActivityHistory/{qid}")
    public ResponseEntity<List<QuestionActivityHistoryDTO>> getQuestionActivityHistory(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<QuestionActivityHistory> al = questionService.getQuestionActivityHistory(qid);
        List<QuestionActivityHistoryDTO> dtoList = new ArrayList<>();
        for (QuestionActivityHistory a : al){
            QuestionActivityHistoryDTO dto = new QuestionActivityHistoryDTO();
            dto.setQuestionActivityHistory(a);
            dto.setUser(userService.findByUid(a.getUid()));
            dtoList.add(dto);
        }
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/createActivityHistory")
    public ResponseEntity<String> createActivityHistory(@CookieValue("sessionCookie") String ck, @RequestBody QuestionActivityHistory questionActivityHistory)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            questionActivityHistory.setUid(user.getUid());
            questionActivityHistory.setDate(new Date());
            String s = questionService.createActivityHistory(questionActivityHistory);
            return ResponseEntity.ok(s);
        }
    }

    //    ---------- Question Report ----------

    @PostMapping("/report/{qid}")
    public ResponseEntity<String> report(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody String detail) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            QuestionReport report = new QuestionReport();
            report.setUid(user.getUid());
            report.setQid(qid);
            report.setDetail(detail);
            report.setStatus("Pending");
            report.setDate(new Date());
            String s = questionService.report(report);
            return ResponseEntity.ok(s);
        }
    }

    @DeleteMapping("/deleteReport/{rqid}")
    public ResponseEntity<String> deleteReport(@CookieValue("sessionCookie") String ck, @PathVariable("rqid") String rqid) {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String s = questionService.deleteReport(rqid);
            return ResponseEntity.ok(s);
        }
    }

    @GetMapping("/getUserReport")
    public ResponseEntity<List<QuestionReportDTO>> getUserReport(@CookieValue("sessionCookie") String ck) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            List<QuestionReport> rl = questionService.getUserReport(user.getUid());
            List<QuestionReportDTO> dtoList = new ArrayList<>();
            for (QuestionReport r : rl){
                QuestionReportDTO dto = new QuestionReportDTO();
                dto.setQuestionReport(r);
                dto.setUser(userService.findByUid(user.getUid()));
                dtoList.add(dto);
            }
            return ResponseEntity.ok(dtoList);
        }
    }

}
