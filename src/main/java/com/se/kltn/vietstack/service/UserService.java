package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    Firestore db = FirestoreClient.getFirestore();

    public boolean checkEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference ref = db.collection("User");
        Query query = ref.whereEqualTo("Email", email);
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

    public User findByUid(String uid) throws ExecutionException, InterruptedException {
        User user;
        DocumentReference ref = db.collection("User").document(uid);
        ApiFuture<DocumentSnapshot> api = ref.get();
        DocumentSnapshot doc = api.get();
        if(doc.exists()){
            user = doc.toObject(User.class);
            return user;
        }
        return new User();
    }

}
