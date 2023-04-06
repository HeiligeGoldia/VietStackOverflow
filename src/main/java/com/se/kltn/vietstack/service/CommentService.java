package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.comment.Comment;
import com.se.kltn.vietstack.model.comment.CommentReport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public Comment getCommentByUidQid(String uid, String qid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Comment");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(Comment.class);
        }
        return new Comment();
    }

    public String createComment(Comment comment) throws ExecutionException, InterruptedException {
        Comment c = getCommentByUidQid(comment.getUid(), comment.getQid());
        if(c.getCid()==null){
            int newCid = Integer.parseInt(getLastCid()) + 1;
            String cid = String.valueOf(newCid);
            comment.setCid(cid);
            ApiFuture<WriteResult> api = db.collection("Comment").document(comment.getCid()).set(comment);
            api.get();
            return comment.getCid();
        }
        else {
            c.setDate(comment.getDate());
            c.setDetail(comment.getDetail());
            c.setStatus("Modified");
            ApiFuture<WriteResult> api = db.collection("Comment").document(c.getCid()).set(c);
            api.get();
            return c.getCid();
        }
    }

    public List<Comment> getCommentByQid(String qid) throws ExecutionException, InterruptedException {
        List<Comment> cl = new ArrayList<>();
        CollectionReference ref = db.collection("Comment");
        Query query = ref.whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs) {
            cl.add(ds.toObject(Comment.class));
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

    public String report(CommentReport commentReport) throws ExecutionException, InterruptedException {
        int newRcid = Integer.parseInt(getLastRcid()) + 1;
        String rcid = String.valueOf(newRcid);
        commentReport.setRcid(rcid);

        ApiFuture<WriteResult> api = db.collection("CommentReport").document(commentReport.getRcid()).set(commentReport);
        api.get();
        return commentReport.getRcid();
    }

    public List<CommentReport> getUserReport(String uid) throws ExecutionException, InterruptedException {
        List<CommentReport> crl = new ArrayList<>();
        CollectionReference ref = db.collection("CommentReport");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs) {
            crl.add(ds.toObject(CommentReport.class));
        }
        return crl;
    }

    public String deleteReport(String rcid){
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

}
