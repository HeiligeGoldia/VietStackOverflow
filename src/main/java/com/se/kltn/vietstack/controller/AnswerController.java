package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.answer.AnswerDetail;
import com.se.kltn.vietstack.model.answer.AnswerVote;
import com.se.kltn.vietstack.model.dto.AnswerAnswerDetailDTO;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/answer")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private AccountService accountService;

    //    ---------- Answer ----------

    @PostMapping("/create/{qid}")
    public ResponseEntity<String> create(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody Answer answer)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            answer.setUid(user.getUid());
            answer.setQid(qid);
            answer.setDate(new Date());
            answer.setStatus("None");
            String s = answerService.createAnswer(answer);
            return ResponseEntity.ok(s);
        }
    }

    @GetMapping("/getAnswerAndDetailByQid/{qid}")
    public ResponseEntity<List<AnswerAnswerDetailDTO>> getAnswerAndDetailByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<AnswerAnswerDetailDTO> aadtl = new ArrayList<>();
        List<Answer> la = answerService.getAnswerByQid(qid);
        for(Answer a : la){
            List<AnswerDetail> adl = answerService.getAnswerDetailByAid(a.getAid());
            AnswerAnswerDetailDTO dto = new AnswerAnswerDetailDTO();
            dto.setAnswer(a);
            dto.setAnswerDetails(adl);
            aadtl.add(dto);
        }
        return ResponseEntity.ok(aadtl);
    }

    @GetMapping("/getAnswerByQid/{qid}")
    public ResponseEntity<List<Answer>> getAnswerByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<Answer> la = answerService.getAnswerByQid(qid);
        return ResponseEntity.ok(la);
    }

    @PutMapping("/acceptAnswer/{aid}")
    public ResponseEntity<String> acceptAnswer(@CookieValue("sessionCookie") String ck, @PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String s = answerService.acceptAnswer(aid);
            return ResponseEntity.ok(s);
        }
    }

    //    ---------- Answer Detail ----------

    @PostMapping("/createDetail/{aid}")
    public ResponseEntity<String> createDetail(@CookieValue("sessionCookie") String ck, @PathVariable("aid") String aid, @RequestBody List<AnswerDetail> answerDetailList)
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

    @GetMapping("/getAnswerDetailByAid/{aid}")
    public ResponseEntity<List<AnswerDetail>> getAnswerDetailByAid(@PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        List<AnswerDetail> adl = answerService.getAnswerDetailByAid(aid);
        return ResponseEntity.ok(adl);
    }

    //    ---------- Answer Vote ----------

    @PostMapping("/castAnswerVoteUD/{aid}")
    public ResponseEntity<String> castQuestionVoteUD(@CookieValue("sessionCookie") String ck, @PathVariable("aid") String aid, @RequestBody AnswerVote answerVote)
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

    @GetMapping("/getUserVoteValue/{aid}")
    public ResponseEntity<String> getUserVoteValue(@CookieValue("sessionCookie") String ck, @PathVariable("aid") String aid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String val = answerService.getUserVoteValue(user.getUid(), aid);
            return ResponseEntity.ok(val);
        }
    }

    //    ---------- Answer Activity History ----------



    //    ---------- Answer Report ----------



}
