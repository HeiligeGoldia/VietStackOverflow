package com.se.kltn.vietstack.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
import com.se.kltn.vietstack.model.user.Account;
import com.se.kltn.vietstack.model.user.User;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class AccountService {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    Firestore db = FirestoreClient.getFirestore();

    public void adminClaim(String uid) throws FirebaseAuthException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "Admin");
        auth.setCustomUserClaims(uid,claims);
    }

    public void userClaim(String uid) throws FirebaseAuthException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "User");
        auth.setCustomUserClaims(uid,claims);
    }

    public String getUserClaims(String ss) throws FirebaseAuthException {
        FirebaseToken tk = auth.verifySessionCookie(ss, true);
        Map<String, Object> claims = tk.getClaims();
        if(claims.get("role")==null){
            return "No role found";
        }
        else return claims.get("role").toString();
    }

    public String register1(String email) throws FirebaseAuthException {
        Twilio.init("AC96e6a911a4dbdeb1ba8e7d5aaabedd76", "381b33be411e99b13013a1e426a3b750");
        Verification verification = Verification.creator(
                        "VA9a5636ec411f18ae4d68159a9c9518e9",
                email,"email").create();
        return "Email sent";
    }

    public String register2(String email, String otp){
        VerificationCheck verificationCheck = VerificationCheck.creator("VA9a5636ec411f18ae4d68159a9c9518e9", otp)
                .setTo(email)
                .create();
        if(verificationCheck.getStatus().equals("approved")){
            return "Approved";
        }
        else {
            return "OTP invalid";
        }
    }

    public String create(Account account) throws FirebaseAuthException {
        UserRecord.CreateRequest ur = new UserRecord.CreateRequest();
        ur.setEmail(account.getEmail());
        ur.setPassword(account.getPassword());
        ur.setEmailVerified(true);
        String id = auth.createUser(ur).getUid();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "User");
        auth.setCustomUserClaims(id,claims);
        return id;
    }

    public String createSessionCookie(String token) {
        try {
            FirebaseToken decodedToken = auth.verifyIdToken(token);
            long authTimeMillis = TimeUnit.SECONDS.toMillis((long) decodedToken.getClaims().get("auth_time"));
            if (System.currentTimeMillis() - authTimeMillis < TimeUnit.MINUTES.toMillis(1)) {
                long expiresIn = 1123200000;
                SessionCookieOptions options = SessionCookieOptions.builder()
                        .setExpiresIn(expiresIn)
                        .build();
                return auth.createSessionCookie(token, options);
            }
            else return "Session time out";
        } catch (FirebaseAuthException e) {
            return "Token invalid";
        }
    }

    public User verifySC(String ss) {
        User user;
        try{
            FirebaseToken tk = auth.verifySessionCookie(ss, true);
            DocumentReference ref = db.collection("User").document(tk.getUid());
            ApiFuture<DocumentSnapshot> api = ref.get();
            DocumentSnapshot doc = api.get();
            if(doc.exists()){
                user = doc.toObject(User.class);
                return user;
            }
            return new User();
        } catch (FirebaseAuthException e) {
            return new User();
        } catch (ExecutionException e) {
            return new User();
        } catch (InterruptedException e) {
            return new User();
        }
    }

    public String clearSessionCookieById(String uid) {
        try {
            auth.revokeRefreshTokens(uid);
            return "Revoked";
        } catch (FirebaseAuthException e) {
            return "Session cookie invalid";
        }
    }

    public String clearSessionCookieAndRevoke(String cookie) {
        try {
            FirebaseToken decodedToken = auth.verifySessionCookie(cookie);
            auth.revokeRefreshTokens(decodedToken.getUid());
            return "Revoked";
        } catch (FirebaseAuthException e) {
            return "Session cookie invalid";
        }
    }
}
