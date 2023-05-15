package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.tag.Tag;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TagService {

    Firestore db = FirestoreClient.getFirestore();

    public String getLastTid() throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Tag");
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

    public Tag getTagByName(String name) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("Tag");
        Query query = ref.whereEqualTo("name", name);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        for(QueryDocumentSnapshot ds : docs){
            return ds.toObject(Tag.class);
        }
        return new Tag();
    }

    public String addTag(Tag tag) throws ExecutionException, InterruptedException {
        Tag t = getTagByName(tag.getName());
        if(t.getTid()==null){
            int newTid = Integer.parseInt(getLastTid()) + 1;
            String tid = String.valueOf(newTid);
            tag.setTid(tid);

            ApiFuture<WriteResult> api = db.collection("Tag").document(tag.getTid()).set(tag);
            api.get();
            return tag.getTid();
        }
        else {
            return "Tag already exist";
        }
    }

    public String editTag(Tag tag) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> api = db.collection("Tag").document(tag.getTid()).set(tag);
        api.get();
        return tag.getTid();
    }

    public List<Tag> getAllTag() throws ExecutionException, InterruptedException {
        List<Tag> tl = new ArrayList<>();
        CollectionReference ref = db.collection("Tag");
        Query query = ref.whereNotEqualTo("tid", "0");
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(docs.isEmpty()){
            return tl;
        }
        else {
            for (QueryDocumentSnapshot d : docs) {
                tl.add(d.toObject(Tag.class));
            }
            return tl;
        }
    }

    public Tag getTagByTid(String tid) throws ExecutionException, InterruptedException {
        Tag tag;
        DocumentReference ref = db.collection("Tag").document(tid);
        ApiFuture<DocumentSnapshot> api = ref.get();
        DocumentSnapshot doc = api.get();
        if(doc.exists()){
            tag = doc.toObject(Tag.class);
            return tag;
        }
        return new Tag();
    }

    public boolean checkTag(String tid) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("QuestionTag");
        Query query = ref.whereEqualTo("tid", tid);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments();
        if(!docs.isEmpty())
            return false;
        else
            return true;
    }

    public String deleteTag(String tid) {
        try{
            ApiFuture<WriteResult> writeResult = db.collection("Tag").document(tid).delete();
            writeResult.get();
            return "Tag deleted";
        } catch (ExecutionException e) {
            return "Tag not found";
        } catch (InterruptedException e) {
            return "Tag not found";
        }
    }

}
