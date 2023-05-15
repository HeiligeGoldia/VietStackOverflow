package com.se.kltn.vietstack.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.se.kltn.vietstack.model.tag.Tag;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.AccountService;
import com.se.kltn.vietstack.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/getAllTag")
    public ResponseEntity<List<Tag>> getAllTag() throws ExecutionException, InterruptedException {
        List<Tag> tl = tagService.getAllTag();
        return ResponseEntity.ok(tl);
    }

    @GetMapping("/getTagByTid/{tid}")
    public ResponseEntity<Tag> getTagByTid(@PathVariable("tid") String tid) throws ExecutionException, InterruptedException {
        Tag t = tagService.getTagByTid(tid);
        if(t.getTid()==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(t);
        }
        else {
            return ResponseEntity.ok(t);
        }
    }

    @PostMapping("/addTag")
    public ResponseEntity<String> addTag(@CookieValue("sessionCookie") String ck, @RequestBody Tag tag) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            }
            else {
                String s = tagService.addTag(tag);
                return ResponseEntity.ok(s);
            }
        }
    }

    @PutMapping("/editTag/{tid}")
    public ResponseEntity<String> editTag(@CookieValue("sessionCookie") String ck, @PathVariable("tid") String tid, @RequestBody Tag tag) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            }
            else {
                Tag t = tagService.getTagByName(tag.getName());
                if(t.getTid()==null || t.getTid().equals(tid)) {
                    tag.setTid(tid);
                    String s = tagService.editTag(tag);
                    return ResponseEntity.ok(s);
                }
                else {
                    return ResponseEntity.ok("Tag already exist");
                }
            }
        }
    }

    @DeleteMapping("/deleteTag/{tid}")
    public ResponseEntity<String> deleteTag(@CookieValue("sessionCookie") String ck, @PathVariable("tid") String tid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            } else {
                if(tagService.checkTag(tid)) {
                    String s =tagService.deleteTag(tid);
                    return ResponseEntity.ok(s);
                }
                else {
                    return ResponseEntity.ok("Tag is in use");
                }
            }
        }
    }

    @GetMapping("/getTagByName")
    public ResponseEntity<Tag> getTagByName(@RequestBody Tag tag) throws ExecutionException, InterruptedException {
        Tag t = tagService.getTagByName(tag.getName());
        return ResponseEntity.ok(t);
    }

}
