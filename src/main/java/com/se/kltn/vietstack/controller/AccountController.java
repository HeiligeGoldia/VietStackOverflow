package com.se.kltn.vietstack.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.se.kltn.vietstack.model.Account;
import com.se.kltn.vietstack.model.User;
import com.se.kltn.vietstack.model.dto.AccountUserDTO;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register1(@RequestBody Account account) throws FirebaseAuthException, ExecutionException, InterruptedException {
        if(userService.checkEmail(account.getEmail())){
            accountService.register1(account.getEmail());
            return "OTP sent";
        }
        else {
            return "Email already in use";
        }
    }

    @PostMapping("/register/{otp}")
    public String register2(@RequestBody AccountUserDTO au, @PathVariable("otp") String otp) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Account account = au.getAccount();
        User user = au.getUser();
        String stt = accountService.register2(account.getEmail(), otp);
        if(stt.equals("Approved")){
            String id = accountService.create(account);
            user.setUid(id);
            user.setEmail(account.getEmail());
            user.setRole("User");
            userService.create(user);
            return id;
        }
        else return stt;
    }

    @PostMapping("/createSessionCookie")
    public String createSessionCookie(@RequestBody String token) throws FirebaseAuthException {
        return accountService.createSessionCookie(token);
//        fix loi do cac session da duoc tao tu truoc
    }

    @PostMapping("/test")
    public String test(@RequestBody String cookie) throws FirebaseAuthException {
        return accountService.test(cookie);
    }

}
