package com.springboot.microservice.example.authorization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocalDBController { 
	
	static String localDBPath = 
			"/home/sep/Desktop/src_microservice/spring-boot-microservice-authorization-service/src/main/resources/local-db.txt";
	
	@GetMapping("/localdb")
	String retrieveLocalDB() {
		return readFile(localDBPath);
	}
	
	
	
	private String readFile(String filePath) {
		
		String content = "";
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			while (line != null) {
					content += line + "\n";
					line = reader.readLine();

			}
			reader.close();
		} catch (IOException e) { 
			e.printStackTrace(); 
		}
		return content;
	}

}
