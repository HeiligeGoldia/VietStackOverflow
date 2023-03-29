package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.question.Question;
import com.se.kltn.vietstack.model.question.QuestionDetail;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

@Service
public class QuestionService {

    Firestore db = FirestoreClient.getFirestore();

    public String create(Question question) throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(question.getDate());
        calendar.add(Calendar.HOUR_OF_DAY, -7);
        question.setDate(calendar.getTime());
        ApiFuture<WriteResult> api = db.collection("Question").document(question.getQid()).set(question);
        api.get();
        return question.getQid();
    }

    public String createDetail(QuestionDetail questionDetail){

        return "";
    }

}
