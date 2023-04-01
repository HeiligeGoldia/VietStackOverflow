package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.question.QuestionDetail;
import com.se.kltn.vietstack.model.question.QuestionTag;
import com.se.kltn.vietstack.model.question.QuestionVote;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.AnswerService;
import com.se.kltn.vietstack.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    //    ---------- Question Tag ----------

    @PostMapping("/addTagToPost/{qid}")
    // kiem tra trung lap cac tag da them
    // xoa cac tag khi chinh sua cau hoi
    public ResponseEntity<String> addTagToPost(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody List<QuestionTag> tags)
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
                for(QuestionTag qt : tags){
                    qt.setQid(qid);
                    questionService.addTagToPost(qt);
                    i++;
                }
                return ResponseEntity.ok(String.valueOf(i));
            }
        }
    }

    @GetMapping("/getQuestionTagByQid/{qid}")
    public ResponseEntity<List<QuestionTag>> getQuestionTagByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<QuestionTag> qtl = questionService.getQuestionTagByQid(qid);
        return ResponseEntity.ok(qtl);
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



    //    ---------- Question Report ----------



}
