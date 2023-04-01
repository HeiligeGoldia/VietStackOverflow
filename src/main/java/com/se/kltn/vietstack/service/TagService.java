package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.tag.Tag;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TagService {

    Firestore db = FirestoreClient.getFirestore();

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

}
