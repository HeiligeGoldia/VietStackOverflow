package com.se.kltn.vietstack.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.se.kltn.vietstack.model.answer.Answer;
import com.se.kltn.vietstack.model.answer.AnswerReport;
import com.se.kltn.vietstack.model.comment.Comment;
import com.se.kltn.vietstack.model.comment.CommentReport;
import com.se.kltn.vietstack.model.dto.QuestionActivityHistoryDTO;
import com.se.kltn.vietstack.model.dto.QuestionDTO;
import com.se.kltn.vietstack.model.dto.QuestionReportDTO;
import com.se.kltn.vietstack.model.question.*;
import com.se.kltn.vietstack.model.tag.Tag;
import com.se.kltn.vietstack.model.user.FollowTag;
import com.se.kltn.vietstack.model.user.User;
import com.se.kltn.vietstack.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    //    ---------- Question ----------

    @PostMapping("/create")
    public ResponseEntity<String> create(@CookieValue("sessionCookie") String ck, @RequestBody Question question) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            question.setUid(user.getUid());
            question.setStatus("Open");
            question.setDate(new Date());
            String s = questionService.create(question);
            return ResponseEntity.ok(s);
        }
    }

    @PutMapping("/edit/{qid}")
    public ResponseEntity<String> edit(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody Question question)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Question q = questionService.getQuestionByQid(qid);
            if(!q.getUid().equals(user.getUid())){
                return ResponseEntity.ok("Access denied");
            }
            else {
                q.setTitle(question.getTitle());
                String s = questionService.edit(q);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getAllQuestionList")
    public ResponseEntity<List<Question>> getAllQuestionList() throws ExecutionException, InterruptedException {
        List<Question> ql = questionService.getAllQuestionList();
        return ResponseEntity.ok(ql);
    }

    @GetMapping("/getQuestionById/{qid}")
    public ResponseEntity<Question> getQuestionById(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        Question q = questionService.getQuestionByQid(qid);
        if(q.getQid()==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(q);
        }
        else {
            return ResponseEntity.ok(q);
        }
    }

    @GetMapping("/checkUserAnswer/{qid}")
    public ResponseEntity<String> checkUserAnswer(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Answer a = answerService.getAnswerByUidQid(user.getUid(),qid);
            if(a.getAid()!=null){
                return ResponseEntity.ok(a.getAid());
            }
            else {
                return ResponseEntity.ok("None");
            }
        }
    }

    @PutMapping("/closeQuestion/{qid}")
    public ResponseEntity<String> closeQuestion(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            Question q = questionService.getQuestionByQid(qid);
            if(!q.getUid().equals(user.getUid()) && !role.equals("Admin")){
                return ResponseEntity.ok("Access denied");
            }
            else {
                List<Answer> al = answerService.getAcceptAnswerByQid(qid);
                if(al.isEmpty()){
                    String s = questionService.closeQuestion(qid, false);
                    return ResponseEntity.ok(s);
                }
                else {
                    String s = questionService.closeQuestion(qid, true);
                    return ResponseEntity.ok(s);
                }
            }
        }
    }

    @GetMapping("/findQuestion")
    public ResponseEntity<List<QuestionDTO>> findQuestion(@RequestBody String input) throws ExecutionException, InterruptedException {
        List<Integer> ids1 = questionService.getSearchQuestionTitle(input);
        List<Integer> ids2 = questionService.getSearchQuestionDetail(input);
        List<Integer> ids3 = answerService.getSearchAnswerDetail(input);

        for(Integer id1 : ids2){
            if(!ids1.contains(id1)){
                ids1.add(id1);
            }
        }
        for(Integer id2 : ids3){
            if(!ids1.contains(id2)){
                ids1.add(id2);
            }
        }
        Collections.sort(ids1, Collections.reverseOrder());

        List<QuestionDTO> dtoList = new ArrayList<>();
        for(Integer id : ids1){
            QuestionDTO questionDTO = new QuestionDTO();
            Question q = questionService.getQuestionByQid(String.valueOf(id));
            List<Tag> tags = new ArrayList<>();
            List<QuestionTag> qtags = questionService.getQuestionTagByQid(q.getQid());
            for (QuestionTag qt : qtags){
                tags.add(tagService.getTagByTid(qt.getTid()));
            }
            int qv = questionService.getTotalVoteValue(q.getQid());
            int ac = answerService.getTotalAnswerCountByQid(q.getQid());
            List<Answer> acpa = answerService.getAcceptAnswerByQid(q.getQid());
            if(!acpa.isEmpty()){
                questionDTO.setAcceptAnswerAvailable(true);
            }
            User u = userService.findByUid(q.getUid());
            questionDTO.setQuestion(q);
            questionDTO.setTags(tags);
            questionDTO.setQuestionVote(qv);
            questionDTO.setAnswerCount(ac);
            questionDTO.setUser(u);
            dtoList.add(questionDTO);
        }

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/getAllQuestionDTO")
    public ResponseEntity<List<QuestionDTO>> getAllQuestionDTO() throws ExecutionException, InterruptedException {
        List<QuestionDTO> dtoList = new ArrayList<>();
        List<Question> questions = questionService.getAllQuestionList();
        for (Question q : questions){
            QuestionDTO questionDTO = new QuestionDTO();
            List<Tag> tags = new ArrayList<>();
            List<QuestionTag> qtags = questionService.getQuestionTagByQid(q.getQid());
            for (QuestionTag qt : qtags){
                 tags.add(tagService.getTagByTid(qt.getTid()));
            }
            int qv = questionService.getTotalVoteValue(q.getQid());
            int ac = answerService.getTotalAnswerCountByQid(q.getQid());
            List<Answer> acpa = answerService.getAcceptAnswerByQid(q.getQid());
            if(!acpa.isEmpty()){
                questionDTO.setAcceptAnswerAvailable(true);
            }
            User u = userService.findByUid(q.getUid());
            questionDTO.setQuestion(q);
            questionDTO.setTags(tags);
            questionDTO.setQuestionVote(qv);
            questionDTO.setAnswerCount(ac);
            questionDTO.setUser(u);
            dtoList.add(questionDTO);
        }
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/getQuestionDTOByUid/{uid}")
    public ResponseEntity<List<QuestionDTO>> getQuestionDTOByUid(@PathVariable("uid") String uid) throws ExecutionException, InterruptedException {
        List<QuestionDTO> dtoList = new ArrayList<>();
        List<Question> questions = questionService.getQidByUid(uid);
        for (Question q : questions){
            QuestionDTO questionDTO = new QuestionDTO();
            List<Tag> tags = new ArrayList<>();
            List<QuestionTag> qtags = questionService.getQuestionTagByQid(q.getQid());
            for (QuestionTag qt : qtags){
                tags.add(tagService.getTagByTid(qt.getTid()));
            }
            int qv = questionService.getTotalVoteValue(q.getQid());
            int ac = answerService.getTotalAnswerCountByQid(q.getQid());
            List<Answer> acpa = answerService.getAcceptAnswerByQid(q.getQid());
            if(!acpa.isEmpty()){
                questionDTO.setAcceptAnswerAvailable(true);
            }
            User u = userService.findByUid(q.getUid());
            questionDTO.setQuestion(q);
            questionDTO.setTags(tags);
            questionDTO.setQuestionVote(qv);
            questionDTO.setAnswerCount(ac);
            questionDTO.setUser(u);
            dtoList.add(questionDTO);
        }
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/getQuestionDTOByTag/{tid}")
    public ResponseEntity<List<QuestionDTO>> getQuestionDTOByTag(@PathVariable("tid") String tid) throws ExecutionException, InterruptedException {
        List<QuestionDTO> dtoList = new ArrayList<>();
        List<Question> questions = questionService.getQidByTid(tid);
        for (Question q : questions){
            QuestionDTO questionDTO = new QuestionDTO();
            List<Tag> tags = new ArrayList<>();
            List<QuestionTag> qtags = questionService.getQuestionTagByQid(q.getQid());
            for (QuestionTag qt : qtags){
                tags.add(tagService.getTagByTid(qt.getTid()));
            }
            int qv = questionService.getTotalVoteValue(q.getQid());
            int ac = answerService.getTotalAnswerCountByQid(q.getQid());
            List<Answer> acpa = answerService.getAcceptAnswerByQid(q.getQid());
            if(!acpa.isEmpty()){
                questionDTO.setAcceptAnswerAvailable(true);
            }
            User u = userService.findByUid(q.getUid());
            questionDTO.setQuestion(q);
            questionDTO.setTags(tags);
            questionDTO.setQuestionVote(qv);
            questionDTO.setAnswerCount(ac);
            questionDTO.setUser(u);
            dtoList.add(questionDTO);
        }
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/getQuestionDTOByUserTag")
    public ResponseEntity<List<QuestionDTO>> getQuestionDTOByUserTag(@CookieValue("sessionCookie") String ck) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            List<QuestionDTO> dtoList = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            List<FollowTag> ftl = userService.getFollowTagByUid(user.getUid());
            for(FollowTag ft : ftl) {
                List<Question> questions = questionService.getQidByTid(ft.getTid());
                for(Question q : questions) {
                    if(!ids.contains(Integer.parseInt(q.getQid()))) {
                        ids.add(Integer.parseInt(q.getQid()));
                    }
                }
            }
            Collections.sort(ids, Collections.reverseOrder());

            for (Integer id : ids){
                QuestionDTO questionDTO = new QuestionDTO();
                Question q = questionService.getQuestionByQid(String.valueOf(id));
                List<Tag> tags = new ArrayList<>();
                List<QuestionTag> qtags = questionService.getQuestionTagByQid(q.getQid());
                for (QuestionTag qt : qtags){
                    tags.add(tagService.getTagByTid(qt.getTid()));
                }
                int qv = questionService.getTotalVoteValue(q.getQid());
                int ac = answerService.getTotalAnswerCountByQid(q.getQid());
                List<Answer> acpa = answerService.getAcceptAnswerByQid(q.getQid());
                if(!acpa.isEmpty()){
                    questionDTO.setAcceptAnswerAvailable(true);
                }
                User u = userService.findByUid(q.getUid());
                questionDTO.setQuestion(q);
                questionDTO.setTags(tags);
                questionDTO.setQuestionVote(qv);
                questionDTO.setAnswerCount(ac);
                questionDTO.setUser(u);
                dtoList.add(questionDTO);
            }
            return ResponseEntity.ok(dtoList);
        }
    }

    @GetMapping("/getTotalQuestion")
    public ResponseEntity<Integer> getTotalQuestion(@CookieValue("sessionCookie") String ck)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if (user.getUid() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                int sl = questionService.getSlQuestionTotal();
                return ResponseEntity.ok(sl);
            }
        }
    }

    @GetMapping("/getTotalQuestionYear/{year}")
    public ResponseEntity<HashMap> getTotalQuestionYear(@CookieValue("sessionCookie") String ck, @PathVariable("year") int year)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if (user.getUid() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                HashMap hm = questionService.getSlQuestionInYear(year);
                return ResponseEntity.ok(hm);
            }
        }
    }

    @DeleteMapping("/delete/{qid}")
    public ResponseEntity<String> delete(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Question q = questionService.getQuestionByQid(qid);
            String role = accountService.getUserClaims(ck);
            if (!q.getUid().equals(user.getUid()) && !role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            }
            else {
                List<Answer> al = answerService.getAnswerByQid(qid);
                for(Answer a : al) {
                    List<AnswerReport> arl = answerService.getReportByAid(a.getAid());
                    for(AnswerReport ar : arl) {
                        ar.setAid("Câu trả lời đã bị xoá");
                        ar.setStatus("Đã xoá");
                        answerService.editReport(ar);
                    }
                    answerService.deleteHistoryByAid(a.getAid());
                    answerService.removeAnswerVoteByAid(a.getAid());
                    answerService.removeAllDetailByAid(a.getAid());
                    answerService.deleteAnswer(a.getAid());
                }

                List<Comment> cl = commentService.getCommentByQid(qid);
                for(Comment c : cl) {
                    List<CommentReport> crl = commentService.getReportByCid(c.getCid());
                    for(CommentReport cr : crl) {
                        cr.setCid("Bình luận đã bị xoá");
                        cr.setStatus("Đã xoá");
                        commentService.editReport(cr);
                    }
                    commentService.deleteComment(c.getCid());
                }

                List<QuestionReport> qrl = questionService.getAllQuestionReportByQid(qid);
                for(QuestionReport qr : qrl){
                    qr.setQid("Câu hỏi đã bị xoá");
                    qr.setStatus("Đã xoá");
                    questionService.editReport(qr);
                }
                questionService.deleteHistoryByQid(qid);
                questionService.removeQuestionVoteByQid(qid);
                questionService.removeAllDetailByQid(qid);
                questionService.removeTagsByQid(qid);
                String s = questionService.delete(qid);

                return ResponseEntity.ok(s);
            }
        }
    }

    //    ---------- Question Tag ----------

    @PostMapping("/modifyTagPost/{qid}")
    public ResponseEntity<String> addTagToPost(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody List<String> tags)
            throws ExecutionException, InterruptedException {
        if(tags.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Tag list empty");
        }
        else {
            User user = accountService.verifySC(ck);
            if(user.getUid()==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
            }
            else {
                int i = 0;
                int y = 0;
                List<QuestionTag> tag = questionService.getQuestionTagByQid(qid);
                List<String> tagIdOld = new ArrayList<>();
                for(QuestionTag qt1 : tag){
                    tagIdOld.add(qt1.getTid());
                }

                List<QuestionTag> newTags = new ArrayList<>();
                for(String s1 : tags){
                    if(!tagIdOld.contains(s1)){
                        QuestionTag obj1 = new QuestionTag();
                        obj1.setTid(s1);
                        newTags.add(obj1);
                    }
                }
                List<String> removedTags = new ArrayList<>();
                for(String s2 : tagIdOld){
                    if(!tags.contains(s2)){
                        removedTags.add(s2);
                    }
                }

                for (QuestionTag qt1 : newTags){
                    qt1.setQid(qid);
                    questionService.addTagToPost(qt1);
                    i++;
                }
                for (String qt2 : removedTags){
                    QuestionTag rmt = questionService.getQuestionTagByQidTid(qid, qt2);
                    questionService.removeQuestionTag(rmt);
                    y++;
                }
                return ResponseEntity.ok("Added tag(s): " + i + " - Removed tag(s): " + y);
            }
        }
    }

    @GetMapping("/getQuestionTagByQid/{qid}")
    public ResponseEntity<List<Tag>> getQuestionTagByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<Tag> tags = new ArrayList<>();
        List<QuestionTag> qtl = questionService.getQuestionTagByQid(qid);
        for (QuestionTag qt : qtl){
            tags.add(tagService.getTagByTid(qt.getTid()));
        }
        return ResponseEntity.ok(tags);
    }

    //    ---------- Question Detail ----------

    @PostMapping("/createDetail/{qid}")
    public ResponseEntity<String> createDetail(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody List<QuestionDetail> questionDetailList)
            throws ExecutionException, InterruptedException {
        if(questionDetailList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Detail list empty");
        }
        else {
            User user = accountService.verifySC(ck);
            if(user.getUid()==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
            }
            else {
                int i = 0;
                for(QuestionDetail qd : questionDetailList){
                    qd.setQid(qid);
                    questionService.createDetail(qd);
                    i++;
                }
                return ResponseEntity.ok(String.valueOf(i));
            }
        }
    }

    @PostMapping("/editDetail/{qid}")
    public ResponseEntity<String> editDetail(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody List<QuestionDetail> questionDetailList)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(questionDetailList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Detail list empty");
        }
        else {
            if(user.getUid()==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
            }
            else {
                Question q = questionService.getQuestionByQid(qid);
                if(!q.getUid().equals(user.getUid())){
                    return ResponseEntity.ok("Access denied");
                }
                else {
                    questionService.removeAllDetailByQid(qid);
                    int i = 0;
                    for(QuestionDetail qd : questionDetailList){
                        qd.setQid(qid);
                        questionService.createDetail(qd);
                        i++;
                    }
                    return ResponseEntity.ok(String.valueOf(i));
                }
            }
        }
    }

    @GetMapping("/getQuestionDetailByQid/{qid}")
    public ResponseEntity<List<QuestionDetail>> getQuestionDetailByQid(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<QuestionDetail> qdl = questionService.getQuestionDetailByQid(qid);
        return ResponseEntity.ok(qdl);
    }

    //    ---------- Question Vote ----------

    @PostMapping("/castQuestionVoteUD/{qid}")
    public ResponseEntity<String> castQuestionVoteUD(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody QuestionVote questionVote)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            questionVote.setQid(qid);
            questionVote.setUid(user.getUid());
            String s = questionService.castQuestionVote(questionVote);
            return ResponseEntity.ok(s);
        }
    }

    @GetMapping("/getTotalVoteValue/{qid}")
    public ResponseEntity<String> getTotalVoteValue(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        int i = questionService.getTotalVoteValue(qid);
        return ResponseEntity.ok(String.valueOf(i));
    }

    @GetMapping("/getUserVoteValue/{qid}")
    public ResponseEntity<String> getUserVoteValue(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String val = questionService.getUserVoteValue(user.getUid(), qid);
            return ResponseEntity.ok(val);
        }
    }

    //    ---------- Question Activity History ----------

    @GetMapping("/getQuestionActivityHistory/{qid}")
    public ResponseEntity<List<QuestionActivityHistoryDTO>> getQuestionActivityHistory(@PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        List<QuestionActivityHistory> al = questionService.getQuestionActivityHistory(qid);
        List<QuestionActivityHistoryDTO> dtoList = new ArrayList<>();
        for (QuestionActivityHistory a : al){
            QuestionActivityHistoryDTO dto = new QuestionActivityHistoryDTO();
            dto.setQuestionActivityHistory(a);
            dto.setUser(userService.findByUid(a.getUid()));
            dtoList.add(dto);
        }
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/createActivityHistory/{qid}")
    public ResponseEntity<String> createActivityHistory(@CookieValue("sessionCookie") String ck, @PathVariable ("qid") String qid, @RequestBody QuestionActivityHistory questionActivityHistory)
            throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            questionActivityHistory.setUid(user.getUid());
            questionActivityHistory.setQid(qid);
            questionActivityHistory.setDate(new Date());
            String s = questionService.createActivityHistory(questionActivityHistory);
            return ResponseEntity.ok(s);
        }
    }

    //    ---------- Question Report ----------

    @PostMapping("/report/{qid}")
    public ResponseEntity<String> report(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid, @RequestBody QuestionReport report) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            Question q = questionService.getQuestionByQid(qid);
            if(q.getUid().equals(user.getUid())){
                return ResponseEntity.ok("Can not report your own question");
            }
            else {
                report.setUid(user.getUid());
                report.setQid(qid);
                report.setStatus("Đang chờ xử lý");
                report.setDate(new Date());
                String s = questionService.report(report);
                return ResponseEntity.ok(s);
            }
        }
    }

    @GetMapping("/getUserReportValue/{qid}")
    public ResponseEntity<String> getUserReportValue(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            QuestionReport qr = questionService.getReportByUidQid(user.getUid(), qid);
            if(qr.getRqid()==null){
                return ResponseEntity.ok("None");
            }
            else {
                return ResponseEntity.ok(qr.getRqid());
            }
        }
    }

    @PutMapping("/editReport")
    public ResponseEntity<String> editReport(@CookieValue("sessionCookie") String ck, @RequestBody List<String> qrlid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.ok("Access denied");
            } else {
                for(String id : qrlid){
                    QuestionReport q = questionService.getReportByRqid(id);
                    if(!q.getQid().equals("Câu hỏi đã bị xoá")){
                        q.setStatus("Đã xem xét");
                        questionService.editReport(q);
                    }
                }
                return ResponseEntity.ok("Edited");
            }
        }
    }

    @DeleteMapping("/deleteReport")
    public ResponseEntity<String> deleteReport(@CookieValue("sessionCookie") String ck, @RequestBody List<String> ids) throws FirebaseAuthException, ExecutionException, InterruptedException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorize failed");
        }
        else {
            String role = accountService.getUserClaims(ck);
            for(String rqid : ids) {
                QuestionReport qr = questionService.getReportByRqid(rqid);
                if(!qr.getUid().equals(user.getUid()) && !role.equals("Admin")) {
                    return ResponseEntity.ok("Access denied");
                }
                else {
                    questionService.deleteReport(rqid);
                }
            }
            return ResponseEntity.ok("All report deleted");
        }
    }

    @GetMapping("/getQuestionReport")
    public ResponseEntity<List<QuestionReportDTO>> getQuestionReport(@CookieValue("sessionCookie") String ck) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                List<QuestionReport> rl = questionService.getQuestionReport();
                List<QuestionReportDTO> dtoList = new ArrayList<>();
                for (QuestionReport r : rl){
                    QuestionReportDTO dto = new QuestionReportDTO();
                    dto.setQuestionReport(r);
                    dto.setQuestion(questionService.getQuestionByQid(r.getQid()));
                    dto.setUser(userService.findByUid(r.getUid()));
                    dtoList.add(dto);
                }
                return ResponseEntity.ok(dtoList);
            }
        }
    }

    @GetMapping("/getReportByQid/{qid}")
    public ResponseEntity<List<QuestionReportDTO>> getReportByQid(@CookieValue("sessionCookie") String ck, @PathVariable("qid") String qid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                List<QuestionReport> rl = questionService.getAllQuestionReportByQid(qid);
                List<QuestionReportDTO> dtoList = new ArrayList<>();
                for (QuestionReport r : rl){
                    QuestionReportDTO dto = new QuestionReportDTO();
                    dto.setQuestionReport(r);
                    dto.setQuestion(questionService.getQuestionByQid(r.getQid()));
                    dto.setUser(userService.findByUid(r.getUid()));
                    dtoList.add(dto);
                }
                return ResponseEntity.ok(dtoList);
            }
        }
    }

    @GetMapping("/getUserReport/{uid}")
    public ResponseEntity<?> getUserReport(@CookieValue("sessionCookie") String ck, @PathVariable("uid") String uid) throws ExecutionException, InterruptedException, FirebaseAuthException {
        User user = accountService.verifySC(ck);
        if(user.getUid()==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        else {
            String role = accountService.getUserClaims(ck);
            if (!user.getUid().equals(uid) && !role.equals("Admin")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            else {
                List<QuestionReport> rl = questionService.getUserReport(uid);
                if(rl.isEmpty()){
                    return ResponseEntity.ok("List report empty");
                }
                else {
                    List<QuestionReportDTO> dtoList = new ArrayList<>();
                    for (QuestionReport r : rl){
                        QuestionReportDTO dto = new QuestionReportDTO();
                        dto.setQuestionReport(r);
                        dto.setQuestion(questionService.getQuestionByQid(r.getQid()));
                        dto.setUser(userService.findByUid(r.getUid()));
                        dtoList.add(dto);
                    }
                    return ResponseEntity.ok(dtoList);
                }
            }
        }
    }

}
