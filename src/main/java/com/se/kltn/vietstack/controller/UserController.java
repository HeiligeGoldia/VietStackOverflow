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

    @GetMapping("/getAllUser")
    private ResponseEntity<List<User>> getAllUser() throws ExecutionException, InterruptedException {
        List<User> ul = userService.getAllUser();
        return ResponseEntity.ok(ul);
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

    @PostMapping("/modifyFollowTags")
    public ResponseEntity<String> modifyFollowTags(@CookieValue("sessionCookie") String ck, @RequestBody List<Tag> tags) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            if(tags.isEmpty()){
                String s = userService.removeAllUserFollowTag(user.getUid());
                return ResponseEntity.ok(s);
            }
            else {
                int i = 0;
                int y = 0;
                List<FollowTag> tag = userService.getFollowTagByUid(user.getUid());
                List<String> tagIdOld = new ArrayList<>();
                for (FollowTag qt1 : tag){
                    tagIdOld.add(qt1.getTid());
                }
                List<String> tagIdNew = new ArrayList<>();
                for(Tag qt2 : tags){
                    tagIdNew.add(qt2.getTid());
                }

                List<FollowTag> newTags = new ArrayList<>();
                for(String s1 : tagIdNew){
                    if(!tagIdOld.contains(s1)){
                        FollowTag obj1 = new FollowTag();
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

                for (FollowTag t1 : newTags){
                    t1.setUid(user.getUid());
                    userService.addFollowTag(t1);
                    i++;
                }
                for (String t2 : removedTags){
                    FollowTag rmt = userService.getFollowTagByUidTid(user.getUid(), t2);
                    userService.removeFollowTag(rmt);
                    y++;
                }
                return ResponseEntity.ok("Added tag(s): " + i + " - Removed tag(s): " + y);
            }
        }
    }

    @PostMapping("/modifyFollowTag/{tid}")
    public ResponseEntity<String> modifyFollowTag(@CookieValue("sessionCookie") String ck, @PathVariable("tid") String tid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            FollowTag ft = userService.getFollowTagByUidTid(user.getUid(), tid);
            if(ft.getTfid()==null){
                FollowTag fta = new FollowTag();
                fta.setUid(user.getUid());
                fta.setTid(tid);
                String s = userService.addFollowTag(fta);
                return ResponseEntity.ok(s);
            }
            else {
                String s = userService.removeFollowTag(ft);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/checkFollowTag/{tid}")
    public ResponseEntity<String> checkFollowTag(@CookieValue("sessionCookie") String ck, @PathVariable("tid") String tid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            FollowTag ft = userService.getFollowTagByUidTid(user.getUid(), tid);
            if(ft.getTfid()==null){
                return ResponseEntity.ok("Not following");
            }
            else {
                return ResponseEntity.ok("Following");
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
