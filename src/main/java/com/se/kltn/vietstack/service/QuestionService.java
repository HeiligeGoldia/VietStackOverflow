package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.question.QuestionDetail;
import com.se.kltn.vietstack.model.question.QuestionTag;
import com.se.kltn.vietstack.model.question.QuestionVote;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class QuestionService {

    Firestore db = FirestoreClient.getFirestore();

    //    ---------- Question ----------

    public String getLastQid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Question");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public String create(Question question) throws ExecutionException, InterruptedException {
        int newQid = Integer.parseInt(getLastQid()) + 1;
        String qid = String.valueOf(newQid);
        question.setQid(qid);

        ApiFuture<WriteResult> api = db.collection("Question").document(question.getQid()).set(question);
        api.get();
        return question.getQid();
    }

    public List<Question> getAllQuestionList() throws ExecutionException, InterruptedException {
        List<Question> ql = new ArrayList<>();
        CollectionReference ref = db.collection("Question");
        Query query = ref.whereNotEqualTo("qid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return ql;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                ql.add(d.toObject(Question.class));
            }
            return ql;
        }
    }

    public Question getQuestionByQid(String qid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Question");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot d : querySnapshot.get().getDocuments()) {
            return d.toObject(Question.class);
        }
        return new Question();
    }

    //    ---------- Question Tag ----------

    public String getLastTqid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionTag");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public String addTagToPost(QuestionTag tag) throws ExecutionException, InterruptedException {
        int newTqid = Integer.parseInt(getLastTqid()) + 1;
        String tqid = String.valueOf(newTqid);
        tag.setTqid(tqid);

        ApiFuture<WriteResult> api = db.collection("QuestionTag").document(tag.getTqid()).set(tag);
        api.get();
        return tag.getTqid();
    }

    public List<QuestionTag> getQuestionTagByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionTag> qtl = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionTag");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qtl;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                qtl.add(d.toObject(QuestionTag.class));
            }
            return qtl;
        }
    }

    //    ---------- Question Detail ----------

    public String getLastQdid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionDetail");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public String createDetail(QuestionDetail questionDetail) throws ExecutionException, InterruptedException {
        int newQdid = Integer.parseInt(getLastQdid()) + 1;
        String qdid = String.valueOf(newQdid);
        questionDetail.setQdid(qdid);

        ApiFuture<WriteResult> api = db.collection("QuestionDetail").document(questionDetail.getQdid()).set(questionDetail);
        api.get();
        return questionDetail.getQdid();
    }

    public List<QuestionDetail> getQuestionDetailByQid(String qid) throws ExecutionException, InterruptedException {
        List<QuestionDetail> qdl = new ArrayList<>();
        CollectionReference ref = db.collection("QuestionDetail");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return qdl;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                qdl.add(d.toObject(QuestionDetail.class));
            }
            return qdl;
        }
    }

    //    ---------- Question Vote ----------

    public String getLastVqid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionVote");
        ApiFuture<QuerySnapshot> api = ref.get();
        QuerySnapshot doc = api.get();
        List<QueryDocumentSnapshot> docs = doc.getDocuments();
        if(docs.size() == 0){
            return "0";
        }
        else{
            List<Integer> docId = new ArrayList<>();
            for(QueryDocumentSnapshot ds : docs){
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            return String.valueOf(docId.get(docId.size()-1));
        }
    }

    public QuestionVote getQuestionVoteByUidQid(String uid, String qid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionVote");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(QuestionVote.class);
        }
        return new QuestionVote();
    }

    public String createQuestionVote(QuestionVote questionVote) throws ExecutionException, InterruptedException {
        QuestionVote qv = getQuestionVoteByUidQid(questionVote.getUid(), questionVote.getQid());
        if(qv.getVqid()==null){
            int newVqid = Integer.parseInt(getLastVqid()) + 1;
            String vqid = String.valueOf(newVqid);
            questionVote.setVqid(vqid);
            ApiFuture<WriteResult> api = db.collection("QuestionVote").document(questionVote.getVqid()).set(questionVote);
            api.get();
            return questionVote.getVqid();
        }
        else {
            qv.setValue(questionVote.getValue());
            ApiFuture<WriteResult> api = db.collection("QuestionVote").document(qv.getVqid()).set(qv);
            api.get();
            return qv.getVqid();
        }
    }

    public int getTotalVoteValue(String qid) throws ExecutionException, InterruptedException {
        int totalValue = 0;
        CollectionReference ref = db.collection("QuestionVote");
        Query query = ref.whereEqualTo("qid", qid).whereIn("value", Arrays.asList("Up", "Down"));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            if(ds.toObject(QuestionVote.class).getValue().equals("Up")){
                totalValue++;
            }
            else if(ds.toObject(QuestionVote.class).getValue().equals("Down")){
                totalValue--;
            }
        }
        return totalValue;
    }

    //    ---------- Question Activity History ----------



    //    ---------- Question Report ----------



}
