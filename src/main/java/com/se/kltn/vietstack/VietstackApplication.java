package com.se.kltn.vietstack;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@SpringBootApplication
public class VietstackApplication {

	public static void main(String[] args) throws IOException {

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

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("https://dobakien.github.io/DoAnTotNghiep/")
						.allowCredentials(true).allowedMethods("POST", "GET", "OPTIONS", "DELETE")
						.allowedHeaders("Content-Type", "Accept", "X-Requested-With", "remember-me", "Cookie");
			}
		};
	}

}
