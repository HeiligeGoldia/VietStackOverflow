package com.se.kltn.vietstack.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.se.kltn.vietstack.model.user.Account;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.model.dto.AccountUserDTO;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

//    @GetMapping("/getUserClaims")
//    public ResponseEntity<String> getUserClaims(@CookieValue("sessionCookie") String ck) throws FirebaseAuthException {
//        User user = accountService.verifySC(ck);
//        if(user.getUid()==null){
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
//        }
//        else {
//            return ResponseEntity.ok(accountService.getUserClaims(ck));
//        }
//    }
//
//    @PostMapping("/adminClaim/{uid}")
//    public ResponseEntity<String> adminClaim(@PathVariable("uid") String uid) throws FirebaseAuthException {
//        accountService.adminClaim(uid);
//        return ResponseEntity.ok("Admin role granted");
//    }
//
//    @PostMapping("/userClaim/{uid}")
//    public ResponseEntity<String> userClaim(@PathVariable("uid") String uid) throws FirebaseAuthException {
//        accountService.userClaim(uid);
//        return ResponseEntity.ok("User role granted");
//    }

    @PostMapping("/register")
    public ResponseEntity<String> register1(@RequestBody Account account) throws FirebaseAuthException, ExecutionException, InterruptedException {
        if(userService.checkEmail(account.getEmail())){
            accountService.register1(account.getEmail());
            return ResponseEntity.ok("OTP sent");
        }
        else {
            return ResponseEntity.ok("Email already in use");
        }
    }

    @PostMapping("/register/{otp}")
    public ResponseEntity<String> register2(@RequestBody AccountUserDTO au, @PathVariable("otp") String otp) throws FirebaseAuthException, ExecutionException, InterruptedException {
        Account account = au.getAccount();
        User user = au.getUser();
        String stt = accountService.register2(account.getEmail(), otp);
        if(stt.equals("Approved")){
            String id = accountService.create(account);
            user.setUid(id);
            user.setEmail(account.getEmail());
            userService.create(user);
            return ResponseEntity.ok(id);
        }
        else return ResponseEntity.ok(stt);
    }

    @PostMapping("/createSessionCookie")
    public ResponseEntity<String> createSessionCookie(@RequestBody String token) {
        String s = accountService.createSessionCookie(token);
        return ResponseEntity.ok(s);
    }

    @PostMapping("/verifySC")
    public ResponseEntity<User> verifySC(@CookieValue("sessionCookie") String ck) {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(user);
        }
        else return ResponseEntity.ok(user);
    }

    @PostMapping("/clearSessionCookieById/{uid}")
    public ResponseEntity<String> clearSessionCookieById(@PathVariable("uid") String uid){
        String s = accountService.clearSessionCookieById(uid);
        return ResponseEntity.ok(s);
    }

    @PostMapping("/clearSessionCookieAndRevoke")
    public ResponseEntity<String> clearSessionCookieAndRevoke(@CookieValue("sessionCookie") String ck){
        String s = accountService.clearSessionCookieAndRevoke(ck);
        return ResponseEntity.ok(s);
    }

}
