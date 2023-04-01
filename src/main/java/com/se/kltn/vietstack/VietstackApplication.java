package com.se.kltn.vietstack;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootApplication
public class VietstackApplication {

	public static void main(String[] args) throws IOException {

		ClassLoader classLoader = VietstackApplication.class.getClassLoader();
		File file = new File((classLoader.getResource("vietstack-kltn2023-firebase-adminsdk.json")).getFile());
		FileInputStream firebase_serv = new FileInputStream(file.getAbsolutePath());

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(firebase_serv))
				.build();

		FirebaseApp.initializeApp(options);

		SpringApplication.run(VietstackApplication.class, args);
	}

}
