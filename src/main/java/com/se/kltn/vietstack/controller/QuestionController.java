package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.Question;
import com.se.kltn.vietstack.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/create")
    public String test(@RequestBody Question question) throws ExecutionException, InterruptedException {
        return questionService.create(question);
    }

}
