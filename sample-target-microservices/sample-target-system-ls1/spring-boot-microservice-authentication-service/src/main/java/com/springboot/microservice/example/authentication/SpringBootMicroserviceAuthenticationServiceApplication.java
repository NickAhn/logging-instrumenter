package com.springboot.microservice.example.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients("com.springboot.microservice.example.authentication")
public class SpringBootMicroserviceAuthenticationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootMicroserviceAuthenticationServiceApplication.class, args);
	}

}
