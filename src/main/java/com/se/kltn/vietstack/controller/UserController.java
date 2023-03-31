package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //    ---------- User ----------

    @GetMapping("/findByUid/{uid}")
    public ResponseEntity<User> findByUid(@PathVariable("uid") String uid) throws ExecutionException, InterruptedException {
        User user = userService.findByUid(uid);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(user);
        }
        else {
            return ResponseEntity.ok(user);
        }
    }

    //    ---------- Save ----------



    //    ---------- Follow Tag ----------



}
