package com.se.kltn.vietstack;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@SpringBootApplication
public class VietstackApplication {

	public static void main(String[] args) throws IOException {

//		ClassLoader classLoader = VietstackApplication.class.getClassLoader();
//		File file = new File((classLoader.getResource("vietstack-kltn2023-firebase-adminsdk.json")).getFile());
//		FileInputStream firebase_serv = new FileInputStream(file.getAbsolutePath());

		URL url = new URL("https://drive.google.com/uc?export=download&id=17CRc3fe7UczVpjEo3uLesbtLpQ3B-GTW");
		URLConnection connection = url.openConnection();
		InputStream firebase_serv = connection.getInputStream();

		File tempFile = File.createTempFile("firebase_key", ".json");
		tempFile.deleteOnExit();
		FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = firebase_serv.read(buffer)) != -1) {
			fileOutputStream.write(buffer, 0, bytesRead);
		}
		fileOutputStream.close();
		firebase_serv.close();

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(GoogleCredentials.fromStream(new FileInputStream(tempFile)))
				.build();

		FirebaseApp.initializeApp(options);

		SpringApplication.run(VietstackApplication.class, args);
	}

}
