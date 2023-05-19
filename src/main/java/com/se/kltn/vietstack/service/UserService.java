package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.user.FollowTag;
import com.se.kltn.vietstack.model.user.Save;
import com.se.kltn.vietstack.model.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    Firestore db = FirestoreClient.getFirestore();

    //    ---------- User ----------

    public boolean checkEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("User");
        Query query = ref.whereEqualTo("email", email);
        List<QueryDocumentSnapshot> querySnapshot = query.get().get().getDocuments();
        if(querySnapshot.isEmpty()){
            return true;
        }
        else return false;
    }

    public User create(User user) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("User").document(user.getUid()).set(user);
        api.get();
        return user;
    }

    public User findByUid(String uid) {
        User user;
        try {
            DocumentReference ref = db.collection("User").document(uid);
            ApiFuture<DocumentSnapshot> api = ref.get();
            DocumentSnapshot doc = api.get();
            if (doc.exists()) {
                user = doc.toObject(User.class);
                return user;
            }
            return new User();
        } catch (Exception e) {
            return new User();
        }
    }

    public String updateInfo(User user) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("User").document(user.getUid()).set(user);
        api.get();
        return user.getUid();
    }

    public List<User> getAllUser() throws ExecutionException, InterruptedException {
        List<User> ul = new ArrayList<>();
        CollectionReference ref = db.collection("User");
        Query query = ref.whereNotEqualTo("uid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return ul;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                ul.add(d.toObject(User.class));
            }
            return ul;
        }
    }

    //    ---------- Save ----------

    public String getLastSid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Save");
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

    public Save getSavedQuestionByUidQid(String uid, String qid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Save");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("qid", qid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(Save.class);
        }
        return new Save();
    }

    public String removeSavedQuestion(Save save) {
        try{
            ApiFuture<WriteResult> writeResult = db.collection("Save").document(save.getSid()).delete();
            writeResult.get();
            return "Unsaved";
        } catch (ExecutionException e) {
            return "Saved question not found";
        } catch (InterruptedException e) {
            return "Saved question not found";
        }
    }

    public String saveQuestion(Save save) throws ExecutionException, InterruptedException {
        Save s = getSavedQuestionByUidQid(save.getUid(), save.getQid());
        if(s.getSid()==null){
            int newSid = Integer.parseInt(getLastSid()) + 1;
            String sid = String.valueOf(newSid);
            save.setSid(sid);
            ApiFuture<WriteResult> api = db.collection("Save").document(save.getSid()).set(save);
            api.get();
            return save.getSid();
        }
        else {
            return removeSavedQuestion(s);
        }
    }

    public List<Save> getUserSavedQuestion(String uid) throws ExecutionException, InterruptedException {
        List<Save> sl = new ArrayList<>();
        CollectionReference ref = db.collection("Save");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();

        List<Integer> docId = new ArrayList<>();
        for(QueryDocumentSnapshot ds : docs) {
            docId.add(Integer.parseInt(ds.getId()));
        }
        Collections.sort(docId);

        for(Integer i : docId) {
            sl.add(ref.document(String.valueOf(i)).get().get().toObject(Save.class));
        }
        return sl;
    }

    //    ---------- Follow Tag ----------

    public String getLastTfid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("FollowTag");
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

    public String addFollowTag(FollowTag tag) throws ExecutionException, InterruptedException {
        int newTfid = Integer.parseInt(getLastTfid()) + 1;
        String tfid = String.valueOf(newTfid);
        tag.setTfid(tfid);

        ApiFuture<WriteResult> api = db.collection("FollowTag").document(tag.getTfid()).set(tag);
        api.get();
        return tag.getTfid();
    }

    public List<FollowTag> getFollowTagByUid(String uid) throws ExecutionException, InterruptedException {
        List<FollowTag> ftl = new ArrayList<>();
        CollectionReference ref = db.collection("FollowTag");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return ftl;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                ftl.add(d.toObject(FollowTag.class));
            }
            return ftl;
        }
    }

    public FollowTag getFollowTagByUidTid(String uid, String tid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("FollowTag");
        Query query = ref.whereEqualTo("uid", uid).whereEqualTo("tid", tid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(FollowTag.class);
        }
        return new FollowTag();
    }

    public String removeAllUserFollowTag(String uid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("FollowTag");
        Query query = ref.whereEqualTo("uid", uid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return "Empty";
        }
        else {
            for(QueryDocumentSnapshot ds : docs){
                removeFollowTag(ds.toObject(FollowTag.class));
            }
            return "Clear";
        }
    }

    public String removeFollowTag(FollowTag followTag) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> writeResult = db.collection("FollowTag").document(followTag.getTfid()).delete();
        writeResult.get();
        return "Tag removed";
    }

}
