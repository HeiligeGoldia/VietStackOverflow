package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.comment.AnswerComment;
import com.se.kltn.vietstack.model.comment.AnswerCommentReport;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class AnswerCommentService {

    Firestore db = FirestoreClient.getFirestore();

    //    ---------- Answer Comment ----------

    public String getLastCaid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerComment");
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

    public AnswerComment getAnswerCommentByCaid(String caid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerComment");
        Query query = ref.whereEqualTo("caid", caid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(AnswerComment.class);
        }
        return new AnswerComment();
    }

    public String createAnswerComment(AnswerComment answerComment) throws ExecutionException, InterruptedException {
        int newCaid = Integer.parseInt(getLastCaid()) + 1;
        String caid = String.valueOf(newCaid);
        answerComment.setCaid(caid);
        ApiFuture<WriteResult> api = db.collection("AnswerComment").document(answerComment.getCaid()).set(answerComment);
        api.get();
        return answerComment.getCaid();
    }

    public String editAnswerComment(AnswerComment answerComment) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("AnswerComment").document(answerComment.getCaid()).set(answerComment);
        api.get();
        return answerComment.getCaid();
    }

    public List<AnswerComment> getAnswerCommentByAid(String aid) throws ExecutionException, InterruptedException {
        List<AnswerComment> cl = new ArrayList<>();
        CollectionReference ref = db.collection("AnswerComment");
        Query query = ref.whereEqualTo("aid", aid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId);

        for(Integer i : docId) {
            cl.add(ref.document(String.valueOf(i)).get().get().toObject(AnswerComment.class));
        }
        return cl;
    }

    public String deleteAnswerComment(String caid){
        try{
            ApiFuture<WriteResult> writeResult = db.collection("AnswerComment").document(caid).delete();
            writeResult.get();
            return "Comment deleted";
        } catch (ExecutionException e) {
            return "Comment not found";
        } catch (InterruptedException e) {
            return "Comment not found";
        }
    }

    public int getSlAnswerCommentTotal() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerComment");
        Query query = ref.whereNotEqualTo("caid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public int getSlAnswerCommentInMonthYear(int month, int year) throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date dateBegin = calendar.getTime();

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        Date dateEnd = calendar.getTime();

        CollectionReference ref = db.collection("AnswerComment");
        Query query = ref
                .whereGreaterThanOrEqualTo("date", dateBegin)
                .whereLessThanOrEqualTo("date", dateEnd);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public HashMap getSlAnswerCommentInYear(int year) throws ExecutionException, InterruptedException {
        HashMap qiy = new HashMap();
        for(int i = 1; i <= 12; i++) {
            int slq = getSlAnswerCommentInMonthYear(i, year);
            qiy.put(i, slq);
        }
        return qiy;
    }

    //    ---------- Answer Comment Report ----------

    public String getLastRcaid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerCommentReport");
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

    public AnswerCommentReport getReportByUidCaid(String uid, String caid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerCommentReport");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("caid", caid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(AnswerCommentReport.class);
        }
        return new AnswerCommentReport();
    }

    public AnswerCommentReport getReportByRcaid(String rcaid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerCommentReport");
        Query query = ref.whereEqualTo("rcaid", rcaid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(AnswerCommentReport.class);
        }
        return new AnswerCommentReport();
    }

    public String report(AnswerCommentReport answerCommentReport) throws ExecutionException, InterruptedException {
        AnswerCommentReport cr = getReportByUidCaid(answerCommentReport.getUid(), answerCommentReport.getCaid());
        if(cr.getRcaid()==null){
            int newRcid = Integer.parseInt(getLastRcaid()) + 1;
            String rcid = String.valueOf(newRcid);
            answerCommentReport.setRcaid(rcid);

            ApiFuture<WriteResult> api = db.collection("AnswerCommentReport").document(answerCommentReport.getRcaid()).set(answerCommentReport);
            api.get();
            return answerCommentReport.getRcaid();
        }
        else {
            return "Already reported";
        }
    }

    public List<AnswerCommentReport> getAnswerCommentReport() throws ExecutionException, InterruptedException {
        List<AnswerCommentReport> crl = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("AnswerCommentReport");
        Query query = ref.whereNotEqualTo("rcaid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return crl;
        }
        else {
            for(QueryDocumentSnapshot ds : docs) {
                docId.add(Integer.parseInt(ds.getId()));
            }
            Collections.sort(docId);
            for(Integer i : docId) {
                crl.add(ref.document(String.valueOf(i)).get().get().toObject(AnswerCommentReport.class));
            }
            return crl;
        }
    }

    public List<AnswerCommentReport> getUserReport(String uid) throws ExecutionException, InterruptedException {
        List<AnswerCommentReport> crl = new ArrayList<>();
        CollectionReference ref = db.collection("AnswerCommentReport");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId);

        for(Integer i : docId) {
            crl.add(ref.document(String.valueOf(i)).get().get().toObject(AnswerCommentReport.class));
        }
        return crl;
    }

    public List<AnswerCommentReport> getReportByCaid(String caid) throws ExecutionException, InterruptedException {
        List<AnswerCommentReport> crl = new ArrayList<>();
        CollectionReference ref = db.collection("AnswerCommentReport");
        Query query = ref.whereEqualTo("caid", caid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId, Collections.reverseOrder());

        for(Integer i : docId) {
            crl.add(ref.document(String.valueOf(i)).get().get().toObject(AnswerCommentReport.class));
        }

        return crl;
    }

    public String editReport(AnswerCommentReport answerCommentReport) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("AnswerCommentReport").document(answerCommentReport.getRcaid()).set(answerCommentReport);
        api.get();
        return answerCommentReport.getRcaid();
    }

    public String deleteReport(String rcaid) {
        try{
            ApiFuture<WriteResult> writeResult = db.collection("AnswerCommentReport").document(rcaid).delete();
            writeResult.get();
            return "Report deleted";
        } catch (ExecutionException e) {
            return "Report not found";
        } catch (InterruptedException e) {
            return "Report not found";
        }
    }

    public int getSlAnswerCommentReportTotal() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("AnswerCommentReport");
        Query query = ref.whereNotEqualTo("rcaid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public int getSlAnswerCommentReportInMonthYear(int month, int year) throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date dateBegin = calendar.getTime();

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        Date dateEnd = calendar.getTime();

        CollectionReference ref = db.collection("AnswerCommentReport");
        Query query = ref
                .whereGreaterThanOrEqualTo("date", dateBegin)
                .whereLessThanOrEqualTo("date", dateEnd);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public HashMap getSlAnswerCommentReportInYear(int year) throws ExecutionException, InterruptedException {
        HashMap qiy = new HashMap();
        for(int i = 1; i <= 12; i++) {
            int slq = getSlAnswerCommentReportInMonthYear(i, year);
            qiy.put(i, slq);
        }
        return qiy;
    }

}
