package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.comment.Comment;
import com.se.kltn.vietstack.model.comment.CommentReport;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class CommentService {

    Firestore db = FirestoreClient.getFirestore();

    //    ---------- Comment ----------

    public String getLastCid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Comment");
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

    public Comment getCommentByCid(String cid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Comment");
        Query query = ref.whereEqualTo("cid", cid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(Comment.class);
        }
        return new Comment();
    }

    public String createComment(Comment comment) throws ExecutionException, InterruptedException {
        int newCid = Integer.parseInt(getLastCid()) + 1;
        String cid = String.valueOf(newCid);
        comment.setCid(cid);
        ApiFuture<WriteResult> api = db.collection("Comment").document(comment.getCid()).set(comment);
        api.get();
        return comment.getCid();
    }

    public String editComment(Comment comment) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("Comment").document(comment.getCid()).set(comment);
        api.get();
        return comment.getCid();
    }

    public List<Comment> getCommentByQid(String qid) throws ExecutionException, InterruptedException {
        List<Comment> cl = new ArrayList<>();
        CollectionReference ref = db.collection("Comment");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId);

        for(Integer i : docId) {
            cl.add(ref.document(String.valueOf(i)).get().get().toObject(Comment.class));
        }
        return cl;
    }

    public String deleteComment(String cid){
        try{
            ApiFuture<WriteResult> writeResult = db.collection("Comment").document(cid).delete();
            writeResult.get();
            return "Comment deleted";
        } catch (ExecutionException e) {
            return "Comment not found";
        } catch (InterruptedException e) {
            return "Comment not found";
        }
    }

    public int getSlCommentTotal() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Comment");
        Query query = ref.whereNotEqualTo("rcid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public int getSlCommentInMonthYear(int month, int year) throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date dateBegin = calendar.getTime();

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        Date dateEnd = calendar.getTime();

        CollectionReference ref = db.collection("Comment");
        Query query = ref
                .whereGreaterThanOrEqualTo("date", dateBegin)
                .whereLessThanOrEqualTo("date", dateEnd);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public HashMap getSlCommentInYear(int year) throws ExecutionException, InterruptedException {
        HashMap qiy = new HashMap();
        for(int i = 1; i <= 12; i++) {
            int slq = getSlCommentInMonthYear(i, year);
            qiy.put(i, slq);
        }
        return qiy;
    }

    //    ---------- Comment Report ----------

    public String getLastRcid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("CommentReport");
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

    public CommentReport getReportByUidCid(String uid, String cid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("CommentReport");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("cid", cid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(CommentReport.class);
        }
        return new CommentReport();
    }

    public CommentReport getReportByRcid(String rcid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("CommentReport");
        Query query = ref.whereEqualTo("rcid", rcid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(CommentReport.class);
        }
        return new CommentReport();
    }

    public String report(CommentReport commentReport) throws ExecutionException, InterruptedException {
        CommentReport cr = getReportByUidCid(commentReport.getUid(), commentReport.getCid());
        if(cr.getRcid()==null){
            int newRcid = Integer.parseInt(getLastRcid()) + 1;
            String rcid = String.valueOf(newRcid);
            commentReport.setRcid(rcid);

            ApiFuture<WriteResult> api = db.collection("CommentReport").document(commentReport.getRcid()).set(commentReport);
            api.get();
            return commentReport.getRcid();
        }
        else {
            return "Already reported";
        }
    }

    public List<CommentReport> getCommentReport() throws ExecutionException, InterruptedException {
        List<CommentReport> crl = new ArrayList<>();
        List<Integer> docId = new ArrayList<>();
        CollectionReference ref = db.collection("CommentReport");
        Query query = ref.whereNotEqualTo("rcid", "0");
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
                crl.add(ref.document(String.valueOf(i)).get().get().toObject(CommentReport.class));
            }
            return crl;
        }
    }

    public List<CommentReport> getUserReport(String uid) throws ExecutionException, InterruptedException {
        List<CommentReport> crl = new ArrayList<>();
        CollectionReference ref = db.collection("CommentReport");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId);

        for(Integer i : docId) {
            crl.add(ref.document(String.valueOf(i)).get().get().toObject(CommentReport.class));
        }
        return crl;
    }

    public List<CommentReport> getReportByCid(String cid) throws ExecutionException, InterruptedException {
        List<CommentReport> crl = new ArrayList<>();
        CollectionReference ref = db.collection("CommentReport");
        Query query = ref.whereEqualTo("cid", cid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId, Collections.reverseOrder());

        for(Integer i : docId) {
            crl.add(ref.document(String.valueOf(i)).get().get().toObject(CommentReport.class));
        }

        return crl;
    }

    public String editReport(CommentReport commentReport) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("CommentReport").document(commentReport.getRcid()).set(commentReport);
        api.get();
        return commentReport.getRcid();
    }

    public String deleteReport(String rcid) {
        try{
            ApiFuture<WriteResult> writeResult = db.collection("CommentReport").document(rcid).delete();
            writeResult.get();
            return "Report deleted";
        } catch (ExecutionException e) {
            return "Report not found";
        } catch (InterruptedException e) {
            return "Report not found";
        }
    }

    public int getSlCommentReportTotal() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("CommentReport");
        Query query = ref.whereNotEqualTo("rcid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public int getSlCommentReportInMonthYear(int month, int year) throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date dateBegin = calendar.getTime();

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        Date dateEnd = calendar.getTime();

        CollectionReference ref = db.collection("CommentReport");
        Query query = ref
                .whereGreaterThanOrEqualTo("date", dateBegin)
                .whereLessThanOrEqualTo("date", dateEnd);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        return docs.size();
    }

    public HashMap getSlCommentReportInYear(int year) throws ExecutionException, InterruptedException {
        HashMap qiy = new HashMap();
        for(int i = 1; i <= 12; i++) {
            int slq = getSlCommentReportInMonthYear(i, year);
            qiy.put(i, slq);
        }
        return qiy;
    }

}
