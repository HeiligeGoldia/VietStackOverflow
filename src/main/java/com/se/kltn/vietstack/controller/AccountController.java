package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.Account;
import com.se.kltn.vietstack.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

//    @GetMapping("/checkEmail/{email}")
//    private String checkEmail(@PathVariable("email")String email){
//        if(accountService.checkEmail(email)){
//            return "null";
//        }
//        else return "existed";
//    }

    @PostMapping("/createAccount")
    private String createAccount(@RequestBody Account account){
        if(accountService.checkEmail(account.getEmail())){
            accountService.createAccount(account);
            return account.getUid();
        }
        else return "existed";
    }

}
