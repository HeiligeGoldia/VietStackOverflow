package com.se.kltn.vietstack.controller;

import com.se.kltn.vietstack.model.tag.Tag;
import com.se.kltn.vietstack.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;

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

}
