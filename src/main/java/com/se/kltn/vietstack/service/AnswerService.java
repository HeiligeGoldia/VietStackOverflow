package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.answer.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class AnswerService {

    Firestore db = FirestoreClient.getFirestore();

    //    ---------- Answer ----------

    public String getLastAid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Answer");
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

    public Answer getAnswerByUidQid(String uid, String qid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Answer");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(Answer.class);
        }
        return new Answer();
    }

    public String createAnswer(Answer answer) throws ExecutionException, InterruptedException {
        Answer a = getAnswerByUidQid(answer.getUid(), answer.getQid());
        if(a.getAid()==null){
            int newAid = Integer.parseInt(getLastAid()) + 1;
            String aid = String.valueOf(newAid);
            answer.setAid(aid);
            ApiFuture<WriteResult> api = db.collection("Answer").document(answer.getAid()).set(answer);
            api.get();
            return answer.getAid();
        }
        else {
            a.setDate(answer.getDate());
            ApiFuture<WriteResult> api = db.collection("Answer").document(a.getAid()).set(a);
            api.get();
            return a.getAid();
        }
    }

    public Answer getAnswerByAid(String aid) throws ExecutionException, InterruptedException {
        Answer answer;
        DocumentReference ref = db.collection("Answer").document(aid);
        ApiFuture<DocumentSnapshot> api = ref.get();
        DocumentSnapshot doc = api.get();
        if(doc.exists()){
            answer = doc.toObject(Answer.class);
            return answer;
        }
        return new Answer();
    }

    public List<Answer> getAnswerByQid(String qid) throws ExecutionException, InterruptedException {
        List<Answer> al = new ArrayList<>();
        CollectionReference ref = db.collection("Answer");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs) {
            al.add(ds.toObject(Answer.class));
        }
        return al;
    }

    public String acceptAnswer(String aid) throws ExecutionException, InterruptedException {
        Answer a = getAnswerByAid(aid);
        if(a.getStatus().equals("None")){
            a.setStatus("Accepted");
        }
        else if(a.getStatus().equals("Accepted")) {
            a.setStatus("None");
        }
        ApiFuture<WriteResult> api = db.collection("Answer").document(a.getAid()).set(a);
        api.get();
        return "Modified";
    }

    public List<Answer> getAcceptAnswerByQid(String qid) throws ExecutionException, InterruptedException {
        List<Answer> al = new ArrayList<>();
        CollectionReference ref = db.collection("Answer");
        Query query = ref.whereEqualTo("qid", qid).whereEqualTo("status", "Accepted");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs) {
            al.add(ds.toObject(Answer.class));
        }
        return al;

    }

    public int getTotalAnswerCountByQid(String qid) throws ExecutionException, InterruptedException {
        CollectionReference collection = db.collection("Answer");
        Query query = collection.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        return querySnapshot.get().size();
    }

    //    ---------- Answer Detail ----------

    public String getLastAdid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerDetail");
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

    public String createDetail(AnswerDetail answerDetail) throws ExecutionException, InterruptedException {
        int newAdid = Integer.parseInt(getLastAdid()) + 1;
        String adid = String.valueOf(newAdid);
        answerDetail.setAdid(adid);

        ApiFuture<WriteResult> api = db.collection("AnswerDetail").document(answerDetail.getAdid()).set(answerDetail);
        api.get();
        return answerDetail.getAdid();
    }

    public List<AnswerDetail> getAnswerDetailByAid(String aid) throws ExecutionException, InterruptedException {
        List<AnswerDetail> adl = new ArrayList<>();
        CollectionReference ref = db.collection("AnswerDetail");
        Query query = ref.whereEqualTo("aid", aid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs) {
            adl.add(ds.toObject(AnswerDetail.class));
        }
        return adl;
    }

    //    ---------- Answer Vote ----------

    public String getLastVaid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerVote");
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

    public AnswerVote getAnswerVoteByUidAid(String uid, String aid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerVote");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("aid", aid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(AnswerVote.class);
        }
        return new AnswerVote();
    }

    public String removeAnswerVote(AnswerVote answerVote) {
        try{
            ApiFuture<WriteResult> writeResult = db.collection("AnswerVote").document(answerVote.getVaid()).delete();
            writeResult.get();
            return "Vote removed";
        } catch (ExecutionException e) {
            return "Vote not found";
        } catch (InterruptedException e) {
            return "Vote not found";
        }
    }

    public String castAnswerVote(AnswerVote answerVote) throws ExecutionException, InterruptedException {
        AnswerVote av = getAnswerVoteByUidAid(answerVote.getUid(), answerVote.getAid());
        if(av.getVaid()==null){
            int newVaid = Integer.parseInt(getLastVaid()) + 1;
            String vaid = String.valueOf(newVaid);
            answerVote.setVaid(vaid);
            ApiFuture<WriteResult> api = db.collection("AnswerVote").document(answerVote.getVaid()).set(answerVote);
            api.get();
            return answerVote.getVaid();
        }
        else {
            if(answerVote.getValue().equals(av.getValue())){
                return removeAnswerVote(av);
            }
            else {
                av.setValue(answerVote.getValue());
                ApiFuture<WriteResult> api = db.collection("AnswerVote").document(av.getVaid()).set(av);
                api.get();
                return av.getVaid();
            }
        }
    }

    public int getTotalVoteValue(String aid) throws ExecutionException, InterruptedException {
        int totalValue = 0;
        CollectionReference ref = db.collection("AnswerVote");
        Query query = ref.whereEqualTo("aid", aid).whereIn("value", Arrays.asList("Up", "Down"));
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            if(ds.toObject(AnswerVote.class).getValue().equals("Up")){
                totalValue++;
            }
            else if(ds.toObject(AnswerVote.class).getValue().equals("Down")){
                totalValue--;
            }
        }
        return totalValue;
    }

    public String getUserVoteValue(String uid, String aid) throws ExecutionException, InterruptedException {
        AnswerVote av = getAnswerVoteByUidAid(uid, aid);
        if(av.getVaid()==null) {
            return "0";
        }
        else {
            return av.getValue();
        }
    }

    //    ---------- Answer Activity History ----------

    public String getLastAahid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerActivityHistory");
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

    public String createActivityHistory(AnswerActivityHistory answerActivityHistory) throws ExecutionException, InterruptedException {
        int newAahid = Integer.parseInt(getLastAahid()) + 1;
        String aahid = String.valueOf(newAahid);
        answerActivityHistory.setAahid(aahid);

        ApiFuture<WriteResult> api = db.collection("AnswerActivityHistory").document(answerActivityHistory.getAahid()).set(answerActivityHistory);
        api.get();
        return answerActivityHistory.getAahid();
    }

    public List<AnswerActivityHistory> getAnswerActivityHistory(String aid) throws ExecutionException, InterruptedException {
        List<AnswerActivityHistory> aal = new ArrayList<>();
        CollectionReference ref = db.collection("AnswerActivityHistory");
        Query query = ref.whereEqualTo("aid", aid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return aal;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                aal.add(d.toObject(AnswerActivityHistory.class));
            }
            return aal;
        }
    }

    //    ---------- Answer Report ----------

    public String getLastRaid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerReport");
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

    public String report(AnswerReport answerReport) throws ExecutionException, InterruptedException {
        int newRaid = Integer.parseInt(getLastRaid()) + 1;
        String raid = String.valueOf(newRaid);
        answerReport.setRaid(raid);

        ApiFuture<WriteResult> api = db.collection("AnswerReport").document(answerReport.getRaid()).set(answerReport);
        api.get();
        return answerReport.getRaid();
    }

    public List<AnswerReport> getUserReport(String uid) throws ExecutionException, InterruptedException {
        List<AnswerReport> arl = new ArrayList<>();
        CollectionReference ref = db.collection("AnswerReport");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs) {
            arl.add(ds.toObject(AnswerReport.class));
        }
        return arl;
    }

    public String deleteReport(String raid){
        try{
            ApiFuture<WriteResult> writeResult = db.collection("AnswerReport").document(raid).delete();
            writeResult.get();
            return "Report deleted";
        } catch (ExecutionException e) {
            return "Report not found";
        } catch (InterruptedException e) {
            return "Report not found";
        }
    }

}
