//package com.se.kltn.vietstack.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//@Configuration
//public class FirebaseConfig {
//
//    @Value("${firebase.projectId}")
//    private String projectId;
//
//    @Value("${firebase.clientEmail}")
//    private String clientEmail;
//
//    @Value("${firebase.privateKey}")
//    private String privateKey;
//
//    @PostConstruct
//    public void initializeFirebaseApp() throws IOException {
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(getFirebaseCredentials())
//                .setProjectId(projectId)
//                .build();
//
//        FirebaseApp.initializeApp(options);
//    }
//
//    private GoogleCredentials getFirebaseCredentials() throws IOException {
//        InputStream serviceAccount = new ByteArrayInputStream(privateKey.getBytes());
//
//        return GoogleCredentials.fromStream(serviceAccount);
//    }
//
//}
