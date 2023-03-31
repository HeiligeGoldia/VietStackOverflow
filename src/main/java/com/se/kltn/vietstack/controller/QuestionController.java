package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.question.QuestionDetail;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AccountService accountService;

    //    ---------- Question ----------

    @PostMapping("/create")
    public String create(@CookieValue("sessionCookie") String ck, @RequestBody Question question) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return "Authorize failed";
        }
        else {
            question.setUid(user.getUid());
            question.setStatus("Open");
            question.setDate(new Date());
            return questionService.create(question);
        }
    }

    //    ---------- Question Tag ----------



    //    ---------- Question Detail ----------

    @PostMapping("/createDetail/{qid}")
    public String createDetail(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody List<QuestionDetail> questionDetailList)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return "Authorize failed";
        }
        else {
            int i = 0;
            for(QuestionDetail qd : questionDetailList){
                qd.setQid(qid);
                questionService.createDetail(qd);
                i++;
            }
            return String.valueOf(i);
        }
    }

    //    ---------- Question Vote ----------



    //    ---------- Question Activity History ----------



    //    ---------- Question Report ----------



}
