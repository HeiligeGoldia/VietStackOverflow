package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.question.QuestionDetail;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
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

    //    ---------- Question Tag ----------



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

    //    ---------- Question Vote ----------



    //    ---------- Question Activity History ----------



    //    ---------- Question Report ----------



}
