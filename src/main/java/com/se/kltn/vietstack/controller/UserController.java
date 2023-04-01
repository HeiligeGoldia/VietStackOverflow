package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.tag.Tag;
import com.se.kltn.vietstack.model.user.FollowTag;
import com.se.kltn.vietstack.model.user.Save;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.QuestionService;
import com.se.kltn.vietstack.service.TagService;
import com.se.kltn.vietstack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TagService tagService;

    @Autowired
    private QuestionService questionService;

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

    @PutMapping("/updateUser")
    public ResponseEntity<String> updateUser(@CookieValue("sessionCookie") String ck, @RequestBody User newUser) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            User u = userService.findByUid(user.getUid());
            newUser.setUid(u.getUid());
            newUser.setEmail(u.getEmail());
            newUser.setRole(u.getRole());
            String s = userService.updateInfo(newUser);
            return ResponseEntity.ok(s);
        }
    }

    //    ---------- Save ----------

    @PostMapping("/saveQuestion/{qid}")
    public ResponseEntity<String> saveQuestion(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Save sv = new Save();
            sv.setUid(user.getUid());
            sv.setQid(qid);
            String s = userService.saveQuestion(sv);
            return ResponseEntity.ok(s);
        }
    }

    @GetMapping("/getUserSavedQuestion")
    public ResponseEntity<List<Question>> getUserSavedQuestion(@CookieValue("sessionCookie") String ck) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            List<Question> ql = new ArrayList<>();
            List<Save> sl = userService.getUserSavedQuestion(user.getUid());
            for(Save s : sl){
                ql.add(questionService.getQuestionByQid(s.getQid()));
            }
            return ResponseEntity.ok(ql);
        }
    }

    //    ---------- Follow Tag ----------

    @PostMapping("/modifyFollowTag")
    // kiem tra trung lap cac tag da them
    // xoa cac tag khi chinh sua
    public ResponseEntity<String> modifyFollowTag(@CookieValue("sessionCookie") String ck, @RequestBody List<Tag> tagList) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            if(tagList.isEmpty()){
                String s = userService.removeAllUserFollowTag(user.getUid());
                return ResponseEntity.ok(s);
            }
            else {
                int i = 0;
                for (Tag t : tagList){
                    FollowTag ft = new FollowTag();
                    ft.setTid(t.getTid());
                    ft.setUid(user.getUid());
                    userService.addFollowTag(ft);
                    i++;
                }
                return ResponseEntity.ok(String.valueOf(i));
            }
        }
    }

    @GetMapping("/getUserFollowTag")
    public ResponseEntity<List<Tag>> getUserFollowTag(@CookieValue("sessionCookie") String ck) throws ExecutionException, InterruptedException {
        List<Tag> tl = new ArrayList<>();
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            List<FollowTag> ftl = userService.getFollowTagByUid(user.getUid());
            if(ftl.isEmpty()){
                return ResponseEntity.ok(tl);
            }
            else {
                for(FollowTag ft : ftl){
                    Tag t = tagService.getTagByTid(ft.getTid());
                    tl.add(t);
                }
                return ResponseEntity.ok(tl);
            }
        }
    }

}
